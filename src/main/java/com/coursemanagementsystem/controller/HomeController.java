package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.CourseService;
import com.coursemanagementsystem.service.EnrollmentService;
import com.coursemanagementsystem.service.ReviewService;
import com.coursemanagementsystem.service.UserService;
import com.coursemanagementsystem.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        // Fetch top 6 courses for featured section
        Page<Course> coursePage = courseService.findCoursesPaged("", 1, 6);
        model.addAttribute("courses", coursePage.getContent());

        // Prepare meta info
        Map<Long, Long> studentCounts = new HashMap<>();
        Map<Long, Double> averageRatings = new HashMap<>();
        Set<Long> wishlistedCourseIds = Collections.emptySet();

        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            if (user != null) {
                wishlistedCourseIds = wishlistService.getWishlistByUser(user).stream()
                        .map(w -> w.getCourse().getId())
                        .collect(Collectors.toSet());
            }
        }

        for (Course course : coursePage.getContent()) {
            studentCounts.put(course.getId(), enrollmentService.countEnrollmentsByCourseId(course.getId()));
            averageRatings.put(course.getId(), reviewService.getAverageRating(course.getId()));
        }

        model.addAttribute("studentCounts", studentCounts);
        model.addAttribute("averageRatings", averageRatings);
        model.addAttribute("wishlistedCourseIds", wishlistedCourseIds);

        return "home";
    }
}
