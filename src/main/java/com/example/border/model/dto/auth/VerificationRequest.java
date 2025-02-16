package com.example.border.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerificationRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(example = "user@example.com")
        String email,
        @NotBlank(message = "Code is required")
        @Size(min = 6, max = 6)
        @Schema(example = "123456")
        String code
) {
}
