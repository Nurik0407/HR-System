package com.example.border.model.dto.message;

import com.example.border.model.enums.MessageType;

import java.util.UUID;

public record MessageRequest(
        UUID senderId,
        UUID recipientId,
        String content,
        MessageType messageType
) {
}
