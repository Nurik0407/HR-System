package com.example.border.service;

import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.Employer;
import com.example.border.model.enums.NotificationType;

public interface NotificationService {

    void createNotification(
            Applicant receiverApplicant, Employer receiverEmployer, String message, NotificationType type);

    void getNotifications();

    void countUnreadNotifications();
}
