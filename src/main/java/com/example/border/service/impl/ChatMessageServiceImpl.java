package com.example.border.service.impl;

import com.example.border.exception.NotFoundException;
import com.example.border.exception.UnauthorizedAccessException;
import com.example.border.model.dto.message.MessageRequest;
import com.example.border.model.dto.message.MessageResponse;
import com.example.border.model.entity.ChatMessage;
import com.example.border.model.entity.ChatRoom;
import com.example.border.model.entity.User;
import com.example.border.repository.ChatMessageRepository;
import com.example.border.service.ChatMessageService;
import com.example.border.service.ChatRoomService;
import com.example.border.service.UserService;
import com.example.border.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final Logger log = LoggerFactory.getLogger(ChatMessageServiceImpl.class);
    private final ChatMessageRepository chatMessageRepository;
    private final UserContext userContext;
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final WebSocketService webSocketService;

    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository, UserContext userContext, ChatRoomService chatRoomService, UserService userService, WebSocketService webSocketService) {
        this.chatMessageRepository = chatMessageRepository;
        this.userContext = userContext;
        this.chatRoomService = chatRoomService;
        this.userService = userService;
        this.webSocketService = webSocketService;
    }

    @Override
    public ChatMessage createChatMessage(UUID chatId, MessageRequest request) {
        User currentUser = userContext.getCurrentUser();
        ChatRoom chatRoom = chatRoomService.findChatById(chatId);
        User recipientUser = userService.findUserById(request.recipientId());

        ChatMessage message = new ChatMessage();
        message.setChatRoom(chatRoom);
        message.setSender(currentUser);
        message.setRecipient(recipientUser);
        message.setContent(request.content());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        message.setMessageType(request.messageType());
        chatRoom.getMessages().add(message);
        ChatMessage chatMessage = chatMessageRepository.save(message);

        chatRoomService.updateLastMessageAndTimestamp(chatRoom, chatMessage.getContent());

        log.info("Message created and saved in chat room {} by user {}", chatId, currentUser.getId());
        return chatMessage;
    }

    @Override
    public int getUnreadMessagesCount(UUID chatRoomId) {
        int unreadCount = chatMessageRepository.getUnreadMessagesCount(chatRoomId);
        log.debug("Unread message count for chat room {}: {}", chatRoomId, unreadCount);
        return unreadCount;
    }

    @Override
    public void findMessagesByChatId(UUID chatRoomId) {
        User currentUser = userContext.getCurrentUser();
        List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessageByChatRoom_IdOrderByTimestampDesc(chatRoomId);

        List<ChatMessage> unreadMessages = chatMessageList.stream()
                .filter(msg -> !msg.getSender().equals(currentUser) && !msg.isRead())
                .peek(msg -> msg.setRead(true))
                .toList();

        if (!unreadMessages.isEmpty()) {
            chatMessageRepository.saveAll(chatMessageList);
            log.info("Marked {} unread messages as read in chat room {}", unreadMessages.size(), chatRoomId);
        }

        List<MessageResponse> messageResponses = chatMessageList.stream()
                .map(this::mapToMessageResponse)
                .toList();

        webSocketService.chatMessages(chatRoomId, messageResponses);
        log.info("Sent {} messages to websocket for chat room {}", messageResponses.size(), chatRoomId);
    }

    @Override
    public String deleteMessage(UUID messageId) {
        User currentUser = userContext.getCurrentUser();

        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        if (chatMessage.getSender().equals(currentUser)) {
            chatMessageRepository.deleteById(messageId);
            log.info("Message with id {} deleted by user {}", messageId, currentUser.getId());
        } else {
            log.warn("User {} tried to delete message with id {}", currentUser.getId(), messageId);
            throw new UnauthorizedAccessException("You cannot delete other people's messages");
        }

        return "Message deleted successfully";
    }

    @Override
    public String updateMessage(UUID messageId, String message) {
        User currentUser = userContext.getCurrentUser();

        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        if (chatMessage.getSender().equals(currentUser)) {
            chatMessage.setContent(message);
            chatMessageRepository.save(chatMessage);
            log.info("Message with id {} updated by user {}", messageId, currentUser.getId());
        } else {
            log.warn("User {} tried to update message with id {}", currentUser.getId(), messageId);
            throw new UnauthorizedAccessException("You cannot update other people's messages");
        }

        findMessagesByChatId(chatMessage.getChatRoom().getId());

        return "Message updated successfully";
    }

    private MessageResponse mapToMessageResponse(ChatMessage chatMessage) {
        return new MessageResponse(
                chatMessage.getId(),
                chatMessage.getSender().getId(),
                chatMessage.getMessageType(),
                chatMessage.getTimestamp(),
                chatMessage.getContent()
        );
    }
}
