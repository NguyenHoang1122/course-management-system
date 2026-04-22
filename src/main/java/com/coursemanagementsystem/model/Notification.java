package com.coursemanagementsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Recipient

    private String title;
    private String message;
    
    @Column(length = 500)
    private String link; // URL to navigate to when clicked
    
    private String type; // e.g., "INFO", "SUCCESS", "WARNING", "DANGER"
    
    private boolean isRead = false;
    
    private LocalDateTime createdAt = LocalDateTime.now();
}
