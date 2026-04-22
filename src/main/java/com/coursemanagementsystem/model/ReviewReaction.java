package com.coursemanagementsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "review_reactions")
@Data
public class ReviewReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Review review;

    private boolean isHelpful = true;
}
