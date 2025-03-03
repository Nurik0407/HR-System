package com.example.border.controller;

import com.example.border.model.dto.message.MessageRequest;
import com.example.border.service.ChatMessageService;
import com.example.border.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Чат", description = "API для работы с чатами и сообщениями")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    public ChatController(ChatRoomService chatRoomService, ChatMessageService chatMessageService) {
        this.chatRoomService = chatRoomService;
        this.chatMessageService = chatMessageService;
    }

    @MessageMapping("/chat/send/{chatId}")
    public void sendMessage(@DestinationVariable UUID chatId, @Payload MessageRequest request) {
        chatRoomService.sendMessage(chatId, request);
    }

    @MessageMapping("/chat/list")
    public void getChatList(@Payload boolean onlyUnread, @Payload String nameSearch) {
        chatRoomService.findChatList(onlyUnread, nameSearch);
    }

    @MessageMapping("/chat/{chatId}/messages")
    public void getMessages(@DestinationVariable UUID chatId) {
        chatMessageService.findMessagesByChatId(chatId);
    }

    @MessageMapping("/chats/unread")
    public void unreadChatsCount() {
        chatRoomService.getUnreadChatCount();
    }

    @PatchMapping("/message/{messageId}")
    @Operation(
            summary = "Обновление сообщения",
            description = "Позволяет обновить текст сообщения по его идентификатору."
    )
    public ResponseEntity<String> updateMessage(@PathVariable UUID messageId, @RequestBody String message) {
        return ResponseEntity.ok(chatMessageService.updateMessage(messageId, message));
    }

    @DeleteMapping("/message/{messageId}")
    @Operation(
            summary = "Удаление сообщения",
            description = "Удаляет сообщение по его идентификатору."
    )
    public ResponseEntity<String> deleteMessage(@PathVariable UUID messageId) {
        return ResponseEntity.ok(chatMessageService.deleteMessage(messageId));
    }
}
