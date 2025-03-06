package com.example.border.controller;

import com.example.border.model.dto.auth.*;
import com.example.border.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/resend-code")
    @Operation(
            summary = "Повторная отправка кода",
            description = "Отправка кода подтверждения повторно на email."
    )
    public ResponseEntity<String> resendCode(@Parameter(
            required = true, example = "user@example.com") @RequestParam String email) {
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/select-role")
    @Operation(
            summary = "Выбор роли пользователя",
            description = "Устанавливает роль пользователя в сессии и перенаправляет его на страницу авторизации через Google OAuth2."
    )
    public String selectRole(@RequestParam String role,
                             HttpSession session) {
        session.setAttribute("ROLE", role.toUpperCase());
        return "redirect:/oauth2/authorization/google";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest request) {
        return ResponseEntity.ok(authService.changePassword(request));
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Забыли пароль",
            description = "Отправляет пользователю письмо с инструкцией по сбросу пароля."
    )
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(authService.processPasswordResetRequest(email));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Сброс пароля",
            description = "Позволяет пользователю установить новый пароль после подтверждения кода."
    )
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
