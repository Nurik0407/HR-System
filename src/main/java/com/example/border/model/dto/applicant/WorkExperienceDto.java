package com.example.border.model.dto.applicant;

import com.example.border.model.enums.Position;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record WorkExperienceDto(
        UUID workExperienceId,

        @NotNull(message = "Position is required")
        Position position,

        @NotBlank(message = "Company name is required")
        @Size(max = 100, message = "Company name must be less than 100 characters")
        String companyName,

        @NotNull(message = "Start date is required")
        @PastOrPresent(message = "Start date must be in the past or present")
        LocalDate startDate,

        LocalDate endDate,

        @NotNull(message = "Current job status is required")
        Boolean currentJob,

        @NotBlank(message = "Skills are required")
        @Size(max = 250, message = "Skills description is too long")
        String skills

) {
}
