package com.coursemanagementsystem.repository.review;

import com.coursemanagementsystem.model.ReviewReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, Long> {
    Optional<ReviewReaction> findByUserIdAndReviewId(Long userId, Long reviewId);
    long countByReviewIdAndIsHelpful(Long reviewId, boolean isHelpful);
}
