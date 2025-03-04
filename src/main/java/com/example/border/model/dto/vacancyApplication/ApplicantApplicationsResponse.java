package com.example.border.model.dto.vacancyApplication;

import com.example.border.model.enums.ApplicationStatus;
import com.example.border.model.enums.Industry;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicantApplicationsResponse(
        UUID employerId,
        UUID vacancyId,
        String logoUrl,
        String name,
        String position,
        Industry industry,
        LocalDateTime dateOfApplication,
        ApplicationStatus status
) {
}
