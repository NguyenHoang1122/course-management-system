package com.coursemanagementsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "persistent_logins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersistentLogin {

    @Id
    @Column(length = 64)
    private String series;

    @NotBlank(message = "User name is required")
    @Column(name = "username", length = 64, nullable = false)
    private String username;

    @NotBlank(message = "Token is required")
    @Column(length = 64, nullable = false)
    private String token;

    @Column(name = "last_used", nullable = false)
    private LocalDateTime lastUsed;
}



