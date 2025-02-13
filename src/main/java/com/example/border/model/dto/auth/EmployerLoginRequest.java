package com.example.border.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployerLoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(example = "user@example.com")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        @Schema(example = "Password123")
        String password
) {
}
