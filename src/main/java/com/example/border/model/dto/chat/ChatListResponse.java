package com.example.border.model.dto.chat;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatListResponse(
        UUID chatId,
        UUID receiverId,
        String profileImageUrl,
        String name,
        String lastMessage,
        LocalDateTime lastMessageTimestamp
) {
}
