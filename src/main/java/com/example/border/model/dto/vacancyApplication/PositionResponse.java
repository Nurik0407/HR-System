package com.example.border.model.dto.vacancyApplication;

import com.example.border.model.enums.ApplicationStatus;

import java.util.UUID;

public record PositionResponse(
        UUID applicationId,
        String position,
        ApplicationStatus status
) {
}
