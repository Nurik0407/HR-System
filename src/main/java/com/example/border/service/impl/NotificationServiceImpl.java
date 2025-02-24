package com.example.border.service.impl;

import com.example.border.model.dto.notification.NotificationsResponse;
import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.Employer;
import com.example.border.model.entity.Notification;
import com.example.border.model.enums.NotificationType;
import com.example.border.repository.NotificationRepository;
import com.example.border.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final WebSocketService webSocketService;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository, WebSocketService webSocketService) {
        this.notificationRepository = notificationRepository;
        this.webSocketService = webSocketService;
    }

    @Override
    public void createNotification(Applicant receiverApplicant, Employer receiverEmployer, String message, NotificationType type) {

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setNotificationType(type);
        UUID receiverId = null;
        if (receiverApplicant != null) {
            notification.setApplicant(receiverApplicant);
            receiverId = receiverApplicant.getId();
        } else if (receiverEmployer != null) {
            notification.setEmployer(receiverEmployer);
            receiverId = receiverEmployer.getId();
        }

        notificationRepository.save(notification);
        webSocketService.notify(receiverId, message);
    }

    @Override
    public List<NotificationsResponse> getNotificationsForEmployer(UUID employerId) {
        return List.of();
    }
}
