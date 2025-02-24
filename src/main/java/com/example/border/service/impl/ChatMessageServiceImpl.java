package com.example.border.service.impl;

import com.example.border.model.dto.vacancyApplication.VacancyApplicationRequest;
import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.ChatRoom;
import com.example.border.model.entity.ChatMessage;
import com.example.border.model.entity.Employer;
import com.example.border.repository.ChatMessageRepository;
import com.example.border.service.ChatMessageService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }


    @Override
    public void createChatToEmployer(UUID employerId, UUID applicantId, VacancyApplicationRequest request) {

    }

    @Override
    public ChatMessage createMessage(ChatRoom chatRoom, Applicant currentApplicant, Employer employer) {

        return new ChatMessage(

        );
    }
}
