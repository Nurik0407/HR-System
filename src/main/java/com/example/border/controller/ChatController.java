package com.example.border.controller;

import com.example.border.model.dto.message.MessageRequest;
import com.example.border.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat/{chatId}")
    public String sendMessage(@DestinationVariable UUID chatId, @Payload MessageRequest request) {
        return chatService.sendMessage(chatId,request);
    }
}
