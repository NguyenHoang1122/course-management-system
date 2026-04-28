package com.coursemanagementsystem.service;

import com.coursemanagementsystem.model.*;
import com.coursemanagementsystem.repository.review.ReviewReactionRepository;
import com.coursemanagementsystem.repository.review.ReviewReportRepository;
import com.coursemanagementsystem.repository.review.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewReactionRepository reactionRepository;

    @Autowired
    private ReviewReportRepository reportRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    public List<Review> getReviewsByCourseId(Long courseId) {
        return reviewRepository.findByCourseId(courseId);
    }

    public double getAverageRating(Long courseId) {
        Double avg = reviewRepository.findAverageRatingByCourseId(courseId);
        return avg == null ? 0 : avg;
    }

    public Map<Integer, Long> getStarDistribution(Long courseId) {
        List<Review> reviews = getReviewsByCourseId(courseId);
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        for (Review review : reviews) {
            int rating = review.getRating();
            distribution.put(rating, distribution.get(rating) + 1);
        }
        return distribution;
    }

    public Optional<Review> getUserReview(Long userId, Long courseId) {
        return reviewRepository.findByUserIdAndCourseId(userId, courseId);
    }

    public Optional<Review> getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    public void saveOrUpdateReview(User user, Course course, Integer rating, String comment) {
        Review review = reviewRepository.findByUserIdAndCourseId(user.getId(), course.getId())
                .orElseGet(Review::new);

        review.setUser(user);
        review.setCourse(course);
        review.setRating(rating);
        review.setComment(comment == null ? "" : comment.trim());

        reviewRepository.save(review);
    }

    public void toggleHelpful(User user, Long reviewId) {
        Optional<ReviewReaction> reactionOpt = reactionRepository.findByUserIdAndReviewId(user.getId(), reviewId);
        if (reactionOpt.isPresent()) {
            reactionRepository.delete(reactionOpt.get());
        } else {
            Review review = reviewRepository.findById(reviewId).orElse(null);
            if (review != null) {
                ReviewReaction reaction = new ReviewReaction();
                reaction.setUser(user);
                reaction.setReview(review);
                reaction.setHelpful(true);
                reactionRepository.save(reaction);

                // Notification: Review Author gets notified when someone likes their review
                if (!review.getUser().getId().equals(user.getId())) {
                    notificationService.createNotification(
                        review.getUser(),
                        "Lượt thích mới",
                        "<b>" + user.getFullName() + "</b> đã thích đánh giá của bạn trong khóa học <b>" + review.getCourse().getTitle() + "</b>",
                        "SUCCESS",
                        "/courses/" + review.getCourse().getId() + "#reviews"
                    );
                }
            }
        }
    }

    public void reportReview(User user, Long reviewId, String reason) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review != null) {
            ReviewReport report = new ReviewReport();
            report.setUser(user);
            report.setReview(review);
            report.setReason(reason);
            reportRepository.save(report);

            // Notification: All Admins get notified when a review is reported
            List<User> admins = userService.findAllAdmins();
            for (User admin : admins) {
                notificationService.createNotification(
                    admin,
                    "Báo cáo đánh giá mới",
                    "<b>" + user.getFullName() + "</b> đã báo cáo một đánh giá trong khóa học <b>" + review.getCourse().getTitle() + "</b>",
                    "DANGER",
                    "/admin/reports"
                );
            }
        }
    }

    public long getHelpfulCount(Long reviewId) {
        return reactionRepository.countByReviewIdAndIsHelpful(reviewId, true);
    }

    public boolean isHelpfulByUser(Long userId, Long reviewId) {
        return reactionRepository.findByUserIdAndReviewId(userId, reviewId).isPresent();
    }

    public List<Review> getTopReviews(int limit) {
        return reviewRepository.findTop3ByOrderByCreatedAtDesc();
    }

    // --- ADMIN METHODS ---

    public List<ReviewReport> getAllReports() {
        // Return reports sorted by newest first
        return reportRepository.findAll().stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        // 1. Delete all reports associated with this review
        // In a real app, we might have reportRepository.deleteByReviewId(reviewId)
        // But for simplicity, we find them and delete
        List<ReviewReport> reports = reportRepository.findAll().stream()
                .filter(r -> r.getReview().getId().equals(reviewId))
                .collect(java.util.stream.Collectors.toList());
        reportRepository.deleteAll(reports);

        // 2. Delete all reactions associated with this review
        List<ReviewReaction> reactions = reactionRepository.findAll().stream()
                .filter(r -> r.getReview().getId().equals(reviewId))
                .collect(java.util.stream.Collectors.toList());
        reactionRepository.deleteAll(reactions);

        // 3. Delete the review itself
        reviewRepository.deleteById(reviewId);
    }

    public void dismissReport(Long reportId) {
        reportRepository.deleteById(reportId);
    }
}
