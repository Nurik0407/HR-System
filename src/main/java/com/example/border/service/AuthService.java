package com.example.border.service;

import com.example.border.model.dto.auth.AuthResponse;
import com.example.border.model.dto.auth.EmployerLoginRequest;
import com.example.border.model.dto.auth.RegisterRequest;
import com.example.border.model.dto.auth.VerificationRequest;
import jakarta.validation.Valid;

public interface AuthService {
    String register(RegisterRequest request);

    AuthResponse login(@Valid EmployerLoginRequest request);

    String resendVerificationCode(String email);

    String confirmUserAccount(VerificationRequest request);
}
