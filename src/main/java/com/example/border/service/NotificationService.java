package com.example.border.service;

import com.example.border.model.dto.notification.NotificationsResponse;
import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.Employer;
import com.example.border.model.enums.NotificationType;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void createNotification(
            Applicant receiverApplicant, Employer receiverEmployer, String message, NotificationType type);

    List<NotificationsResponse> getNotificationsForEmployer(UUID employerId);
}
