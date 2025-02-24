package com.example.border.service;

import com.example.border.model.dto.message.MessageRequest;
import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.ChatRoom;
import com.example.border.model.entity.Employer;

import java.util.UUID;

public interface ChatService {
    ChatRoom getOrCreateChat(Applicant currentApplicant, Employer employer);

    String sendMessage(UUID chatId,MessageRequest request);
}
