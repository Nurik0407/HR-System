package com.example.border.service.impl;

import com.example.border.model.dto.chat.ChatListResponse;
import com.example.border.model.dto.message.MessageResponse;
import com.example.border.model.dto.notification.NotificationsResponse;
import com.example.border.model.entity.ChatMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendMessage(ChatMessage chatMessage) {
        messagingTemplate.convertAndSend(
                "/queue/chat/" + chatMessage.getChatRoom().getId(),
                new MessageResponse(
                        chatMessage.getId(),
                        chatMessage.getSender().getId(),
                        chatMessage.getMessageType(),
                        chatMessage.getTimestamp(),
                        chatMessage.getContent()
                ));
    }

    public void chatList(UUID userId, List<ChatListResponse> chatListResponse) {
        messagingTemplate.convertAndSend(
                "/queue/chats/" + userId,
                chatListResponse
        );
    }

    public void unreadChatCount(int unreadChatsTotal, UUID userId) {
        messagingTemplate.convertAndSend(
                "/queue/chats/unread/" + userId,
                unreadChatsTotal
        );
    }

    public void chatMessages(UUID chatRoomId, List<MessageResponse> chatMessages) {
        messagingTemplate.convertAndSend(
                "/queue/" + chatRoomId + "/messages",
                chatMessages
        );
    }

    public void notify(UUID recipientUserId, List<NotificationsResponse> notificationsResponse) {
        messagingTemplate.convertAndSend(
                "/queue/notifications/" + recipientUserId,
                notificationsResponse);
    }

    public void unreadNotificationsCount(int unreadNotificationsTotal, UUID recipientUserId) {
        messagingTemplate.convertAndSend(
                "/queue/notifications/unread/" + recipientUserId,
                unreadNotificationsTotal
        );
    }
}
