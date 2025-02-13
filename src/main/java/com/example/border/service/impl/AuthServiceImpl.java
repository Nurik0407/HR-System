package com.example.border.service.impl;

import com.example.border.config.helper.CustomUserDetailsService;
import com.example.border.config.jwt.JwtTokenUtil;
import com.example.border.exception.NotFoundException;
import com.example.border.exception.UserAlreadyEnabledException;
import com.example.border.exception.UserAlreadyExistsException;
import com.example.border.exception.VerificationCodeExpiredException;
import com.example.border.model.dto.auth.AuthResponse;
import com.example.border.model.dto.auth.EmployerLoginRequest;
import com.example.border.model.dto.auth.EmployerRegisterRequest;
import com.example.border.model.dto.auth.VerificationRequest;
import com.example.border.model.entity.Employer;
import com.example.border.model.entity.User;
import com.example.border.model.entity.VerificationCode;
import com.example.border.model.enums.Role;
import com.example.border.repository.EmployerRepository;
import com.example.border.repository.UserRepository;
import com.example.border.repository.VerificationCodeRepository;
import com.example.border.service.AuthService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil, AuthenticationManager authenticationManager, CustomUserDetailsService customUserDetailsService, EmployerRepository employerRepository,
                           VerificationCodeRepository verificationCodeRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.employerRepository = employerRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.emailService = emailService;
    }

    @Transactional
    @Override
    public String register(EmployerRegisterRequest request) {
        log.info("Registering user with email: {}", request.email());

        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Attempt to register with already registered email: {}", request.email());
            throw new UserAlreadyExistsException("Email already registered!");
        }

        User user = new User();
        Employer employer = new Employer();

        employer.setName(request.name());
        user.setEmail(request.email());
        user.setRole(Role.EMPLOYER);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(false);

        User savedUser = userRepository.save(user);
        employer.setUser(savedUser);
        employerRepository.save(employer);
        sendVerificationCode(savedUser);

        log.info("User successfully registered with email: {}", request.email());
        return "Verification code sent to: " + request.email();
    }

    @Transactional
    @Override
    public String resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email: " + email + " not found"));

        if (user.isEnabled()) {
            log.warn("Attempt to resend verification code for already activated user: {}", email);
            throw new UserAlreadyEnabledException("User already activated");
        }

        sendVerificationCode(user);
        log.info("Verification code resent to: {}", email);
        return "Verification code successfully resent";
    }

    @Transactional
    @Override
    public String confirmUserAccount(VerificationRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User with email: " + request.email() + " not found"));

        VerificationCode verificationCode = verificationCodeRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("VerificationCode not found"));

        if (verificationCode.isExpired()) {
            log.error("Verification code expired for user: {}", request.email());
            throw new VerificationCodeExpiredException("Verification code expired");
        }

        if (!verificationCode.getCode().equals(request.code())) {
            log.error("Invalid verification code for user: {}", request.email());
            throw new BadCredentialsException("Verification code not matched");
        }

        user.setEnabled(true);
        verificationCodeRepository.deleteByUser(user);
        userRepository.save(user);

        log.info("User successfully activated: {}", request.email());
        return "User successfully activated";
    }

    @Override
    public AuthResponse login(EmployerLoginRequest request) {
        log.info("Logging in user with email: {}", request.email());

        try {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.email());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(), request.password()
                    ));

            if (!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
                log.error("Invalid password for user: {}", request.email());
                throw new BadCredentialsException("Invalid password!");
            }

            final String accessToken = jwtTokenUtil.generateToken(userDetails);
            log.info("JWT token successfully generated for user: {}", request.email());
            return new AuthResponse(accessToken);

        } catch (BadCredentialsException | UsernameNotFoundException e) {
            log.error("Authentication error for the user: {} - {}", request.email(), e.getMessage());
            throw new BadCredentialsException("Invalid email or password!");
        }
    }

    private void sendVerificationCode(User user) {
        Optional<VerificationCode> existingCode = verificationCodeRepository.findByUser((user));

        String code = generateVerificationCode();
        if (existingCode.isPresent()) {
            existingCode.get().setCode(code);
            existingCode.get().setExpirationTime(calculateExpirationTime());
            verificationCodeRepository.save(existingCode.get());

            emailService.sendEmail(user.getEmail(), code);
            log.info("Verification code updated for user: {}", user.getEmail());
        } else {
            VerificationCode verificationCode = new VerificationCode(user, code, calculateExpirationTime());
            verificationCodeRepository.save(verificationCode);
            log.info("New verification code saved for user: {}", user.getEmail());

            emailService.sendEmail(user.getEmail(), code);
        }
    }

    private String generateVerificationCode() {
        int code;
        do {
            code = new SecureRandom().nextInt(999999);
        } while (code == 0);
        return String.format("%06d", code);
    }

    private LocalDateTime calculateExpirationTime() {
        return LocalDateTime.now().plusMinutes(5);
    }
}
