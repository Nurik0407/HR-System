package com.example.border.service;

import com.example.border.model.dto.auth.PasswordChangeRequest;
import com.example.border.model.dto.auth.AuthResponse;
import com.example.border.model.dto.auth.LoginRequest;
import com.example.border.model.dto.auth.RegisterRequest;
import com.example.border.model.dto.auth.VerificationRequest;
import jakarta.validation.Valid;

public interface AuthService {
    String register(RegisterRequest request);

    AuthResponse login(@Valid LoginRequest request);

    String resendVerificationCode(String email);

    String confirmUserAccount(VerificationRequest request);

    String changePassword(PasswordChangeRequest request);
}
