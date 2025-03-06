package com.example.border.model.dto.auth;

public record ResetPasswordRequest (
        String token,
        String newPassword,
        String confirmPassword
){
}
