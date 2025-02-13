package com.example.border.controller;

import com.example.border.model.dto.auth.AuthResponse;
import com.example.border.model.dto.auth.EmployerLoginRequest;
import com.example.border.model.dto.auth.EmployerRegisterRequest;
import com.example.border.model.dto.auth.VerificationRequest;
import com.example.border.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Аутентификация", description = "API для управления аутентификацией пользователей")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Регистрация пользователя",
            description = "Регистрация нового пользователя и отправка кода подтверждения на email."
    )
    public ResponseEntity<String> register(@Valid @RequestBody EmployerRegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/resend-code")
    @Operation(
            summary = "Повторная отправка кода",
            description = "Отправка кода подтверждения повторно на email."
    )
    public ResponseEntity<String> resendCode(@RequestParam String email) {
        return ResponseEntity.ok(authService.resendVerificationCode(email));

    }

    @PostMapping("/confirm")
    @Operation(
            summary = "Подтверждение аккаунта",
            description = "Подтверждение аккаунта с помощью введенного кода."
    )
    public ResponseEntity<?> confirm(@RequestBody VerificationRequest request) {
        return ResponseEntity.ok(authService.confirmUserAccount(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Авторизация",
            description = "Авторизация пользователя с получением JWT токена."
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody EmployerLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
