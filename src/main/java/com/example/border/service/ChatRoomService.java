package com.example.border.service;

import com.example.border.model.dto.message.MessageRequest;
import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.ChatRoom;
import com.example.border.model.entity.Employer;

import java.util.UUID;

public interface ChatRoomService {
    ChatRoom getOrCreateChat(Applicant currentApplicant, Employer employer);

    void sendMessage(UUID chatId, MessageRequest request);

    ChatRoom findChatById(UUID chatId);

    void updateLastMessageAndTimestamp(ChatRoom chatRoom, String content);

    void findChatList(boolean onlyUnread, String nameSearch);

    void getUnreadChatCount();
}
