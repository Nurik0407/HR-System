package com.example.border.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerificationRequest(
        @Email @NotBlank
        String email,
        @NotBlank @Size(min = 6, max = 6)
        String code
) {
}
