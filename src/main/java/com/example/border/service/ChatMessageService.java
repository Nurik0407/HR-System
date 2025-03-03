package com.example.border.service;

import com.example.border.model.dto.message.MessageRequest;
import com.example.border.model.entity.ChatMessage;

import java.util.UUID;

public interface ChatMessageService {

    ChatMessage createChatMessage(UUID chatRoomId, MessageRequest request);

    int getUnreadMessagesCount(UUID id);

    void findMessagesByChatId(UUID chatId);

    String deleteMessage(UUID messageId);

    String updateMessage(UUID messageId, String message);
}
