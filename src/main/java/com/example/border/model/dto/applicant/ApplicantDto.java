package com.example.border.model.dto.applicant;

import com.example.border.model.enums.Country;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record ApplicantDto(
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must be less than 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must be less than 50 characters")
        String lastName,

        String profilePhotoUrl,

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDay,

        @NotNull(message = "Country is required")
        Country country,

        @NotBlank(message = "City is required")
        @Size(max = 50, message = "City name too long")
        String city,

        String address,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+996\\d{9}$", message = "Invalid phone number format")
        String phoneNumber,

        String aboutMe,

        String CVUrl,

        @Valid
        List<EducationDto> educationsResponse,

        @Valid
        List<WorkExperienceDto> workExperiencesResponse
) {
}
