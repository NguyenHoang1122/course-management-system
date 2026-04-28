package com.coursemanagementsystem.service;

import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.model.Wishlist;
import com.coursemanagementsystem.repository.course.CourseRepository;
import com.coursemanagementsystem.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Transactional
    public boolean toggleWishlist(User user, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Optional<Wishlist> existing = wishlistRepository.findByUserAndCourse(user, course);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return false; // Removed
        } else {
            Wishlist wishlist = new Wishlist(user, course);
            wishlistRepository.save(wishlist);
            return true; // Added
        }
    }

    public boolean isWishlisted(User user, Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return false;
        return wishlistRepository.existsByUserAndCourse(user, course);
    }

    public List<Wishlist> getWishlistByUser(User user) {
        return wishlistRepository.findByUser(user);
    }

    public Page<Wishlist> getWishlistByUser(User user, Pageable pageable) {
        return wishlistRepository.findByUser(user, pageable);
    }

    @Transactional
    public void removeFromWishlist(User user, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        wishlistRepository.deleteByUserAndCourse(user, course);
    }

    public long getWishlistCount(User user) {
        return wishlistRepository.countByUser(user);
    }
}
