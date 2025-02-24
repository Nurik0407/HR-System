package com.example.border.model.dto.message;

import com.example.border.model.enums.MessageType;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID messageId,
        UUID senderId,
        MessageType messageType,
        LocalDateTime timestamp,
        String message
) {
}
