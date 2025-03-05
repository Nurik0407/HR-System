package com.example.border.model.dto.employer.candidate;

import com.example.border.model.enums.ApplicationStatus;
import com.example.border.model.enums.Country;
import com.example.border.model.enums.Experience;
import com.example.border.model.enums.Industry;

import java.time.LocalDateTime;
import java.util.UUID;

public record VacancyCandidatesResponse(
        UUID applicantId,
        String firstName,
        String lastName,
        String position,
        Industry industry,
        Experience experience,
        Country country,
        String city,
        LocalDateTime dateOfApplication,
        UUID vacancyApplicationId,
        ApplicationStatus status
) {
}
