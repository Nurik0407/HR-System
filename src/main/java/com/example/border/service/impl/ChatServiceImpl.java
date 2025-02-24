package com.example.border.service.impl;

import com.example.border.exception.NotFoundException;
import com.example.border.model.dto.chat.ChatListResponse;
import com.example.border.model.dto.message.MessageRequest;
import com.example.border.model.entity.*;
import com.example.border.model.enums.Role;
import com.example.border.repository.ChatMessageRepository;
import com.example.border.repository.ChatRepository;
import com.example.border.service.ChatService;
import com.example.border.service.UserService;
import com.example.border.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {

    private final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserContext userContext;
    private final WebSocketService webSocketService;
    private final UserService userService;

    public ChatServiceImpl(ChatRepository chatRepository, ChatMessageRepository chatMessageRepository, UserContext userContext, WebSocketService webSocketService, UserService userService) {
        this.chatRepository = chatRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userContext = userContext;
        this.webSocketService = webSocketService;
        this.userService = userService;
    }


    @Override
    public ChatRoom getOrCreateChat(Applicant currentApplicant, Employer employer) {
        return chatRepository.findByEmployerAndApplicant(employer, currentApplicant)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = new ChatRoom();
                    newChatRoom.setApplicant(currentApplicant);
                    newChatRoom.setEmployer(employer);
                    return chatRepository.save(newChatRoom);
                });
    }

    @Transactional
    @Override
    public String sendMessage(UUID chatId, MessageRequest request) {
        ChatMessage chatMessage = createChatMessage(chatId, request);
        webSocketService.sendMessage(chatMessage);
        notifyChatListUpdate(chatMessage.getRecipient().getId());
        notifyChatListUpdate(chatMessage.getSender().getId());
        return "Successfully sent message";
    }

    private void notifyChatListUpdate(UUID recipientId) {
        User recipientUser = userService.findUserById(recipientId);
        log.info("recipientId: {}", recipientId);
        List<ChatRoom> chatRoomList;
        if (recipientUser.getRole() == Role.APPLICANT) {
            log.info("Получение чатов соискателя");

            Applicant applicant = recipientUser.getApplicant();
            chatRoomList = chatRepository
                    .findChatsByApplicant_IdOrderByLastMessageTimestampDesc(applicant.getId());

            List<ChatListResponse> chatListResponses = chatRoomList.stream()
                    .map(c -> mapToChatListResponse(
                            c, recipientId, applicant.getProfilePhotoUrl(), applicant.getFullName()))
                    .toList();

            webSocketService.notifyChatListUpdate(recipientId, chatListResponses);
            log.info("chatList size for Applicant: {}", chatListResponses.size());
        } else if (recipientUser.getRole() == Role.EMPLOYER) {
            log.info("Получение чатов работодателя");

            Employer employer = recipientUser.getEmployer();
            chatRoomList = chatRepository.
                    findChatsByEmployer_IdOrderByLastMessageTimestampDesc(employer.getId());

            List<ChatListResponse> chatListResponses = chatRoomList.stream()
                    .map(c -> mapToChatListResponse(
                            c, recipientId, employer.getLogoUrl(), employer.getName()))
                    .toList();

            webSocketService.notifyChatListUpdate(recipientId, chatListResponses);

            log.info("chatList size for Employer: {}", chatListResponses.size());
        }
    }

    private ChatRoom findChatById(UUID chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat with ID: %s not found".formatted(chatId)));
    }

    private ChatMessage createChatMessage(UUID chatId, MessageRequest request) {
        User currentUser = userContext.getCurrentUser();
        ChatRoom chatRoom = findChatById(chatId);
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
        chatRoom.setLastMessage(message.getContent());
        chatRoom.setLastMessageTimestamp(LocalDateTime.now());
        chatRepository.save(chatRoom);

        return chatMessage;
    }

    private ChatListResponse mapToChatListResponse(ChatRoom chatRoom, UUID recipientId,
                                                   String profileImageUrl, String name) {
        return new ChatListResponse(
                chatRoom.getId(),
                recipientId,
                profileImageUrl,
                name,
                chatRoom.getLastMessage(),
                chatRoom.getLastMessageTimestamp()
        );
    }
}
