package com.example.border.model.dto.applicant;

import com.example.border.model.enums.EducationLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record EducationDto(
        UUID educationId,

        @NotNull(message = "Education degree is required")
        EducationLevel educationLevel,

        @NotBlank(message = "Institution name is required")
        @Size(max = 200, message = "Institution name must be less than 200 characters")
        String institution,

        @NotNull(message = "Graduation date is required")
        @Past(message = "Graduation date must be in the past")
        LocalDate graduationDate) {
}
