package com.coursemanagementsystem.service;

import com.coursemanagementsystem.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> getNotificationsForUser(Long userId);
    long countUnreadNotifications(Long userId);
    void markAllAsRead(Long userId);
    void createNotification(Long userId, String title, String message, String link, String type);
}