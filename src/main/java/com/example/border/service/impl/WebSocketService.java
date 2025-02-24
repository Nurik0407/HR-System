package com.example.border.service.impl;

import com.example.border.model.dto.chat.ChatListResponse;
import com.example.border.model.dto.message.MessageResponse;
import com.example.border.model.entity.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WebSocketService {

    private final Logger log = LoggerFactory.getLogger(WebSocketService.class);
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendMessage(ChatMessage chatMessage) {
        messagingTemplate.convertAndSend(
                "/topic/chat/" + chatMessage.getChatRoom().getId(),
                new MessageResponse(
                        chatMessage.getId(),
                        chatMessage.getSender().getId(),
                        chatMessage.getMessageType(),
                        chatMessage.getTimestamp(),
                        chatMessage.getContent()
                ));
    }

    public void notifyChatListUpdate(UUID recipientId, List<ChatListResponse> chatListResponse) {
        log.info("notifyChatListUpdate size: {} ", chatListResponse.size());
        log.info("notifyChatListUpdate: {}", chatListResponse);
        log.info("recipientId: {}", recipientId);
        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/chatListUpdate",
                chatListResponse
        );
    }

    public void notify(UUID recipientId, String message) {
        log.info("Notification   message: {}", message);
        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/notifications",
                message);
    }

//    public void notify(UUID recipientId, String message) {
//        messagingTemplate.convertAndSend(
//                "/topic/notification/" + recipientId,
//                message
//        );
//    }
}
