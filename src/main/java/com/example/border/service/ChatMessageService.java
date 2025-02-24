package com.example.border.service;

import com.example.border.model.dto.vacancyApplication.VacancyApplicationRequest;
import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.ChatRoom;
import com.example.border.model.entity.ChatMessage;
import com.example.border.model.entity.Employer;

import java.util.UUID;

public interface ChatMessageService {
    void createChatToEmployer(UUID employerId, UUID applicantId, VacancyApplicationRequest request);

    ChatMessage createMessage(ChatRoom chatRoom, Applicant currentApplicant, Employer employer);
}
