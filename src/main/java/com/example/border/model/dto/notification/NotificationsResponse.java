package com.example.border.model.dto.notification;

import com.example.border.model.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationsResponse(
        String profileImageUrl,
        NotificationType notificationType,
        String message,
        LocalDateTime timestamp) {
}
