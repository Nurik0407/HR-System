package com.example.border.service.impl;

import com.example.border.exception.NotFoundException;
import com.example.border.model.dto.chat.ChatListResponse;
import com.example.border.model.dto.message.MessageRequest;
import com.example.border.model.entity.*;
import com.example.border.model.enums.NotificationType;
import com.example.border.model.enums.Role;
import com.example.border.repository.ChatRoomRepository;
import com.example.border.service.ChatMessageService;
import com.example.border.service.ChatRoomService;
import com.example.border.service.NotificationService;
import com.example.border.service.UserService;
import com.example.border.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    private final Logger log = LoggerFactory.getLogger(ChatRoomServiceImpl.class);
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageService chatMessageService;
    private final WebSocketService webSocketService;
    private final UserService userService;
    private final UserContext userContext;
    private final NotificationService notificationService;

    public ChatRoomServiceImpl(
            ChatRoomRepository chatRoomRepository,
            @Lazy ChatMessageService chatMessageService,
            WebSocketService webSocketService,
            UserService userService, UserContext userContext, NotificationService notificationService) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageService = chatMessageService;
        this.webSocketService = webSocketService;
        this.userService = userService;
        this.userContext = userContext;
        this.notificationService = notificationService;
    }


    @Override
    public ChatRoom getOrCreateChat(Applicant currentApplicant, Employer employer) {
        return chatRoomRepository.findByEmployerAndApplicant(employer, currentApplicant)
                .orElseGet(() -> {
                    log.info("Creating new chat between Applicant: {} and Employer: {}",
                            currentApplicant.getId(), employer.getId());

                    ChatRoom newChatRoom = new ChatRoom();
                    newChatRoom.setApplicant(currentApplicant);
                    newChatRoom.setEmployer(employer);

                    ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);
                    log.info("New chat created with ID: {}", savedChatRoom.getId());

                    chatListUpdate(currentApplicant.getUser().getId(), false, "");
                    chatListUpdate(employer.getUser().getId(), false, "");

                    return savedChatRoom;
                });
    }

    @Transactional
    @Override
    public void sendMessage(UUID chatId, MessageRequest request) {
        log.info("Sending message in chat {} by user {}", chatId, request.senderId());

        ChatMessage chatMessage = chatMessageService.createChatMessage(chatId, request);
        webSocketService.sendMessage(chatMessage);

        log.info("Message sent successfully. Notifying chat list update...");
        chatListUpdate(chatMessage.getRecipient().getId(), false, "");
        chatListUpdate(chatMessage.getSender().getId(), false, "");

        User userRecipient = userService.findUserById(request.recipientId());

        if (userRecipient.getRole() == Role.EMPLOYER) {
            String message = "Вам пришло новое сообщение от " +
                    userRecipient.getEmployer().getName() + " " +
                    "перейдите в чат, чтобы продолжить общение";
            notificationService.createNotification(
                    null,
                    userRecipient.getEmployer(),
                    message,
                    NotificationType.MESSAGE
            );
        } else if (userRecipient.getRole() == Role.APPLICANT) {
            String message = "Вам пришло новое сообщение от " +
                    userRecipient.getApplicant().getFullName() + " " +
                    "перейдите в чат, чтобы продолжить общение";
            notificationService.createNotification(
                    userRecipient.getApplicant(),
                    null,
                    message,
                    NotificationType.MESSAGE
            );
        }

        getUnreadChatCount();
    }


    @Override
    public ChatRoom findChatById(UUID chatId) {
        return chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat with ID: %s not found".formatted(chatId)));
    }

    @Override
    public void updateLastMessageAndTimestamp(ChatRoom chatRoom, String content) {
        chatRoom.setLastMessage(content);
        chatRoom.setLastMessageTimestamp(LocalDateTime.now());
        chatRoomRepository.save(chatRoom);
    }

    @Override
    public void findChatList(boolean onlyUnread, String nameSearch) {
        nameSearch = "%" + nameSearch + "%";
        User currentUser = userContext.getCurrentUser();
        chatListUpdate(currentUser.getId(), onlyUnread, nameSearch);
    }

    @Override
    public void getUnreadChatCount() {
        UUID currentUserId = userContext.getCurrentUser().getId();
        int unreadChatTotal = chatRoomRepository.findUnreadChatCount(currentUserId);
        log.info("Unread chat count: {}", unreadChatTotal);
        webSocketService.unreadChatCount(unreadChatTotal, currentUserId);
    }

    private void chatListUpdate(
            UUID userId,
            boolean onlyUnread,
            String nameSearch
    ) {
        User recipientUser = userService.findUserById(userId);
        log.info("Notifying chat list update for userId: {}", userId);

        List<ChatRoom> chatRoomList;
        String profileImageUrl;
        String name;
        UUID senderId;

        if (recipientUser.getRole() == Role.APPLICANT) {
            Applicant applicant = recipientUser.getApplicant();
            log.info("Fetching chats for applicant: {}", applicant.getFullName());

            chatRoomList = onlyUnread
                    ? chatRoomRepository.findUnreadChatsByApplicantIdAndEmployerName(applicant.getId(), nameSearch)
                    : chatRoomRepository.findChatsByApplicant_IdOrderByLastMessageTimestampDesc(applicant.getId());
            profileImageUrl = applicant.getProfilePhotoUrl();
            name = applicant.getFullName();
            senderId = recipientUser.getId();
        } else if (recipientUser.getRole() == Role.EMPLOYER) {
            Employer employer = recipientUser.getEmployer();
            log.info("Fetching chats for employer: {}", employer.getName());

            chatRoomList = onlyUnread
                    ? chatRoomRepository.findUnreadChatByEmployerIdAndApplicantName(employer.getId(), nameSearch)
                    : chatRoomRepository.findChatsByEmployer_IdOrderByLastMessageTimestampDesc(employer.getId());
            profileImageUrl = employer.getLogoUrl();
            name = employer.getName();
            senderId = recipientUser.getId();
        } else {
            log.warn("User with ID {} has an unexpected role: {}", userId, recipientUser.getRole());
            throw new IllegalArgumentException("User with ID " + userId + " has an unexpected role: " + recipientUser.getRole());
        }

        List<ChatListResponse> chatListResponses = chatRoomList.stream()
                .map(c -> mapToChatListResponse(c, userId, senderId, profileImageUrl, name))
                .toList();

        log.info("Sending chat list update for user {} ({} chats)", userId, chatListResponses.size());
        webSocketService.chatList(userId, chatListResponses);
    }

    private ChatListResponse mapToChatListResponse(ChatRoom chatRoom, UUID recipientId, UUID senderId,
                                                   String profileImageUrl, String name) {
        int unreadCount = chatMessageService.getUnreadMessagesCount(chatRoom.getId());

        return new ChatListResponse(
                chatRoom.getId(),
                recipientId,
                senderId,
                profileImageUrl,
                name,
                chatRoom.getLastMessage(),
                chatRoom.getLastMessageTimestamp(),
                unreadCount
        );
    }
}
