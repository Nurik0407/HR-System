package com.example.border.service;

import com.example.border.model.dto.auth.*;
import jakarta.validation.Valid;

public interface AuthService {
    String register(RegisterRequest request);

    AuthResponse login(@Valid LoginRequest request);

    String resendVerificationCode(String email);

    String confirmUserAccount(VerificationRequest request);

    String changePassword(PasswordChangeRequest request);

    String processPasswordResetRequest(String email);

    String resetPassword(ResetPasswordRequest request);
}
