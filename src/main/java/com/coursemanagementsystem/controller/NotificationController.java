package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.model.Notification;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.NotificationService;
import com.coursemanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getNotifications(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.findByUsername(principal.getName());
        
        List<Notification> notifications = notificationService.getRecentNotifications(user, 10);
        long unreadCount = notificationService.getUnreadCount(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications.stream().map(n -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", n.getId());
            map.put("title", n.getTitle());
            map.put("message", n.getMessage());
            map.put("link", n.getLink());
            map.put("isRead", n.isRead());
            map.put("type", n.getType());
            map.put("createdAt", n.getCreatedAt().toString());
            return map;
        }).collect(Collectors.toList()));
        response.put("unreadCount", unreadCount);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mark-read/{id}")
    public ResponseEntity<?> markRead(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllRead(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.findByUsername(principal.getName());
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }
}
