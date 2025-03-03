package com.example.border.service.impl;

import com.example.border.model.dto.notification.NotificationsResponse;
import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.Employer;
import com.example.border.model.entity.Notification;
import com.example.border.model.entity.User;
import com.example.border.model.enums.NotificationType;
import com.example.border.model.enums.Role;
import com.example.border.repository.NotificationRepository;
import com.example.border.service.NotificationService;
import com.example.border.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;
    private final WebSocketService webSocketService;
    private final UserContext userContext;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository, WebSocketService webSocketService, UserContext userContext) {
        this.notificationRepository = notificationRepository;
        this.webSocketService = webSocketService;
        this.userContext = userContext;
    }

    @Override
    public void createNotification(Applicant receiverApplicant, Employer receiverEmployer, String message, NotificationType type) {

        log.info("Creating notification for {} with type {}", type, message);
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setNotificationType(type);
        notification.setRead(false);

        notificationRepository.save(notification);
        getNotifications();
    }

    @Override
    public void getNotifications() {
        User currentUser = userContext.getCurrentUser();
        Role role = currentUser.getRole();

        log.info("Fetching notifications for user: {} with role: {}", currentUser.getId(), role);

        List<Notification> notifications;
        if (role == Role.EMPLOYER) {
            notifications = notificationRepository.findByEmployer_Id(currentUser.getEmployer().getId(), Sort.by(Sort.Order.desc("createdAt")));
        } else {
            notifications = notificationRepository.findByApplicant_Id(currentUser.getApplicant().getId(), Sort.by(Sort.Order.desc("createdAt")));
        }

        log.info("Found {} notifications", notifications.size());

        List<Notification> unreadNotifications = notifications.stream()
                .filter(n -> !n.isRead())
                .peek(n -> n.setRead(true))
                .toList();

        log.info("Marking {} notifications as read", unreadNotifications.size());

        List<NotificationsResponse> responseList = notifications.stream()
                .map(n -> mapToNotificationsResponse(n, getProfileImageUrl(n, role)))
                .toList();

        if (!unreadNotifications.isEmpty()) {
            notificationRepository.saveAll(unreadNotifications);
        }

        webSocketService.notify(currentUser.getId(), responseList);
    }

    @Override
    public void countUnreadNotifications() {
        User currentUser = userContext.getCurrentUser();
        log.info("Counting unread notifications for user: {}", currentUser.getId());

        int unreadCount = switch (currentUser.getRole()) {
            case ADMIN -> 0;
            case EMPLOYER ->
                    notificationRepository.getCountUnreadNotificationsByEmployerId(currentUser.getEmployer().getId());
            case APPLICANT ->
                    notificationRepository.getCountUnreadNotificationsByApplicantId(currentUser.getApplicant().getId());
        };

        log.info("User {} has {} unread notifications", currentUser.getId(), unreadCount);
        webSocketService.unreadNotificationsCount(unreadCount, currentUser.getId());
    }

    private NotificationsResponse mapToNotificationsResponse(Notification notification, String profileImageUrl) {
        return new NotificationsResponse(
                profileImageUrl,
                notification.getNotificationType(),
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }

    private String getProfileImageUrl(Notification notification, Role role) {
        return (role == Role.EMPLOYER) ? notification.getEmployer().getLogoUrl() : notification.getApplicant().getProfilePhotoUrl();
    }
}
