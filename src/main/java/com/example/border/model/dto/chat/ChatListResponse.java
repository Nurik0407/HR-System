package com.example.border.model.dto.chat;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatListResponse(
        UUID chatId,
        UUID receiverId,
        UUID senderId,
        String profileImageUrl,
        String name,
        String lastMessage,
        LocalDateTime lastMessageTimestamp,
        int unreadMessagesCount
) {
}
