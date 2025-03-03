package com.example.border.controller;

import com.example.border.service.NotificationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @MessageMapping("/user/notifications")
    public void notificationsResponseList() {
         notificationService.getNotifications();
    }

    @MessageMapping("/notifications/unread")
    public void notificationsCount(){
        notificationService.countUnreadNotifications();
    }
}
