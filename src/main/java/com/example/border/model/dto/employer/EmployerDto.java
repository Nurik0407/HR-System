package com.example.border.model.dto.employer;

import com.example.border.model.enums.Country;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.lang.Nullable;

public record EmployerDto(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be less than 100 characters")
        String name,

        String logoUrl,

        @NotBlank(message = "About company is required")
        @Size(max = 1000, message = "About company must be less than 1000 characters")
        String aboutCompany,

        @NotNull(message = "Country is required")
        Country country,

        @NotBlank(message = "City is required")
        @Size(max = 50, message = "City must be less than 50 characters")
        String city,

        String address,

        @Nullable
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+996\\d{9}$", message = "Invalid phone number format")
        String phoneNumber) {
}