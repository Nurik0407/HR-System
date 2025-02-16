package com.example.border.model.dto.auth;

import com.example.border.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Name is required")
        @Schema(example = "O!Bank")
        String name,

        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(example = "user@example.com")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
        @Schema(example = "Password123")
        String password,

        @NotNull(message = "Role is required")
        Role role
) {
        public RegisterRequest(String name, String lastName, String email, String password, Role role) {
                this.name = name;
                this.lastName = (role == Role.EMPLOYER) ? null : lastName;
                this.email = email;
                this.password = password;
                this.role = role;
        }
}
