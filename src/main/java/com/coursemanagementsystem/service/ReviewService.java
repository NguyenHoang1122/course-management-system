package com.coursemanagementsystem.service;

import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.Review;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

	private final ReviewRepository reviewRepository;

	public ReviewService(ReviewRepository reviewRepository) {
		this.reviewRepository = reviewRepository;
	}

	public List<Review> getReviewsByCourseId(Long courseId) {
		return reviewRepository.findByCourseId(courseId);
	}

	public double getAverageRating(Long courseId) {
		Double avg = reviewRepository.findAverageRatingByCourseId(courseId);
		return avg == null ? 0 : avg;
	}

	public Optional<Review> getUserReview(Long userId, Long courseId) {
		return reviewRepository.findByUserIdAndCourseId(userId, courseId);
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
}
