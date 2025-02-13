package com.example.border.service.impl;

import com.example.border.exception.InvalidRoleException;
import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.Employer;
import com.example.border.model.entity.User;
import com.example.border.model.enums.Role;
import com.example.border.repository.ApplicantRepository;
import com.example.border.repository.EmployerRepository;
import com.example.border.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private static final String EMAIL_ATTRIBUTE_KEY = "email";
    private static final String NAME_ATTRIBUTE_KEY = "name";
    private static final String ROLE_SESSION_ATTRIBUTE = "ROLE";
    private static final String DEFAULT_PRINCIPAL_NAME = "email";

    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final ApplicantRepository applicantRepository;

    public CustomOAuth2UserService(UserRepository userRepository,
                                   EmployerRepository employerRepository,
                                   ApplicantRepository applicantRepository) {
        this.userRepository = userRepository;
        this.employerRepository = employerRepository;
        this.applicantRepository = applicantRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) oAuth2User.getAttributes().get(EMAIL_ATTRIBUTE_KEY);
        String name = (String) oAuth2User.getAttributes().get(NAME_ATTRIBUTE_KEY);

        String role = getRoleFromSession();
        log.debug("Extracted role from session: {}", role);

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(email, name, role));

        validateUserRole(user, role);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                DEFAULT_PRINCIPAL_NAME
        );
    }

    private String getRoleFromSession() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
                RequestContextHolder.getRequestAttributes())).getRequest();
        HttpSession session = request.getSession(false);

        return Optional.ofNullable(session)
                .map(s -> (String) s.getAttribute(ROLE_SESSION_ATTRIBUTE))
                .orElseThrow(() -> new InvalidRoleException("Role not found in session"));
    }

    @Transactional
    protected User createNewUser(String email, String name, String role) {
        Role userRole = parseAndValidateRole(role);

        User user = User.builder()
                .email(email)
                .role(userRole)
                .enabled(true)
                .hasPassword(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Created new user with email: {}", email);

        createRoleSpecificEntity(savedUser, userRole, name);
        return savedUser;
    }

    private void createRoleSpecificEntity(User user, Role role, String name) {
        switch (role) {
            case EMPLOYER -> {
                Employer employer = new Employer();
                employer.setName(name);
                employer.setUser(user);
                employerRepository.save(employer);
                log.debug("Created employer entity for user: {}", user.getEmail());
            }
            case APPLICANT -> {
                Applicant applicant = new Applicant();
                applicant.setFirstName(name);
                applicant.setUser(user);
                applicantRepository.save(applicant);
                log.debug("Created applicant entity for user: {}", user.getEmail());
            }
            default -> {
                log.warn("No specific entity created for role: {}", role);
                throw new InvalidRoleException("Unsupported role: " + role);
            }
        }
    }

    private Role parseAndValidateRole(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.error("Invalid role provided: {}", role);
            throw new InvalidRoleException("Invalid role: " + role, ex);
        }
    }

    private void validateUserRole(User user, String requestedRole) {
        if (!user.getRole().name().equalsIgnoreCase(requestedRole)) {
            log.error("User role mismatch. Existing: {}, Requested: {}", user.getRole(), requestedRole);
            throw new InvalidRoleException("Role mismatch for existing user");
        }
    }
}