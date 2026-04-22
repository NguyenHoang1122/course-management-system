package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.Review;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.CourseService;
import com.coursemanagementsystem.service.EnrollmentService;
import com.coursemanagementsystem.service.LessonProgressService;
import com.coursemanagementsystem.service.ReviewService;
import com.coursemanagementsystem.service.UserService;
import com.coursemanagementsystem.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private UserService userService;
    @Autowired
    private LessonProgressService lessonProgressService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private WishlistService wishlistService;

    @GetMapping("")
    public String findAllCourse(@RequestParam(value = "page", defaultValue = "1") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                @RequestParam(value = "sortBy", defaultValue = "newest") String sortBy,
                                Model model, Principal principal) {
        Page<Course> coursePage = courseService.findCoursesFiltered(null, null, null, sortBy, page, size);

        // Calculate total value
        long totalValue = coursePage.getContent().stream()
                .mapToLong(course -> course.getPrice() != null ? course.getPrice().longValue() : 0)
                .sum();

        long totalLessons = coursePage.getContent().stream()
                .mapToLong(course -> course.getLessons() != null ? course.getLessons().size() : 0)
                .sum();

        // Pass student counts and ratings
        java.util.Map<Long, Long> studentCounts = new java.util.HashMap<>();
        java.util.Map<Long, Double> averageRatings = new java.util.HashMap<>();
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

        model.addAttribute("coursePage", coursePage);
        model.addAttribute("studentCounts", studentCounts);
        model.addAttribute("averageRatings", averageRatings);
        model.addAttribute("wishlistedCourseIds", wishlistedCourseIds);
        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("totalItems", coursePage.getTotalElements());
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("totalLessons", totalLessons);
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("pageTitle", "Course List");
        return "course/list";
    }

    @GetMapping("/{id}")
    public String viewCourse(@PathVariable Long id,
                             @RequestParam(value = "reviewSize", defaultValue = "5") int reviewSize,
                             @RequestParam(value = "ratingFilter", required = false) Integer ratingFilter,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             Model model, Principal principal, HttpServletRequest request) {
        Course course = courseService.findByIdWithLessons(id);
        if (course == null) {
            return "redirect:/courses";
        }

        boolean enrolled = false;
        Set<Long> completedLessonIds = Collections.emptySet();
        Review userReview = null;
        User currentUser = null;

        if (principal != null) {
            currentUser = userService.findByUsername(principal.getName());
            if (currentUser != null) {
                enrolled = enrollmentService.isUserEnrolled(currentUser.getId(), id);
                completedLessonIds = lessonProgressService.getCompletedLessonIds(currentUser.getId(), id);
                Optional<Review> currentReview = reviewService.getUserReview(currentUser.getId(), id);
                userReview = currentReview.orElse(null);
                model.addAttribute("isWishlisted", wishlistService.isWishlisted(currentUser, id));
            }
        } else {
            model.addAttribute("isWishlisted", false);
        }

        // Get reviews with filtering and pagination
        List<Review> allReviews = reviewService.getReviewsByCourseId(id);
        
        // Star distribution (always based on all reviews)
        model.addAttribute("starDistribution", reviewService.getStarDistribution(id));
        model.addAttribute("totalReviews", allReviews.size());

        // Filtering
        if (ratingFilter != null) {
            allReviews = allReviews.stream()
                    .filter(r -> r.getRating().equals(ratingFilter))
                    .collect(Collectors.toList());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase().trim();
            allReviews = allReviews.stream()
                    .filter(r -> r.getComment() != null && r.getComment().toLowerCase().contains(lowerKeyword))
                    .collect(Collectors.toList());
        }

        // Pagination (Simple list sublist for Thymeleaf version)
        int totalFiltered = allReviews.size();
        List<Review> paginatedReviews = allReviews.stream()
                .sorted((r1, r2) -> Long.compare(r2.getId(), r1.getId())) // Newest first
                .limit(reviewSize)
                .collect(Collectors.toList());

        // Map helpful counts and user states
        java.util.Map<Long, Long> helpfulCounts = new java.util.HashMap<>();
        java.util.Map<Long, Boolean> userHelpfulStates = new java.util.HashMap<>();
        for (Review r : paginatedReviews) {
            helpfulCounts.put(r.getId(), reviewService.getHelpfulCount(r.getId()));
            if (currentUser != null) {
                userHelpfulStates.put(r.getId(), reviewService.isHelpfulByUser(currentUser.getId(), r.getId()));
            } else {
                userHelpfulStates.put(r.getId(), false);
            }
        }

        model.addAttribute("course", course);
        model.addAttribute("isEnrolled", enrolled);
        model.addAttribute("completedLessonIds", completedLessonIds);
        model.addAttribute("averageRating", reviewService.getAverageRating(id));
        model.addAttribute("reviews", paginatedReviews);
        model.addAttribute("reviewSize", reviewSize);
        model.addAttribute("totalFiltered", totalFiltered);
        model.addAttribute("ratingFilter", ratingFilter);
        model.addAttribute("keyword", keyword);
        model.addAttribute("helpfulCounts", helpfulCounts);
        model.addAttribute("userHelpfulStates", userHelpfulStates);
        model.addAttribute("userReview", userReview);
        model.addAttribute("studentCount", enrollmentService.countEnrollmentsByCourseId(id));
        
        // Check if it's an AJAX request
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "course/detail :: reviews-list-fragment";
        }
        
        return "course/detail";
    }

    @PostMapping("/{id}/reviews")
    public Object submitReview(@PathVariable Long id,
                               @RequestParam("rating") Integer rating,
                               @RequestParam(value = "comment", required = false) String comment,
                               Principal principal,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        if (rating == null || rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("reviewError", "Rating phai trong khoang 1-5 sao.");
            return "redirect:/courses/" + id;
        }

        User user = userService.findByUsername(principal.getName());
        if (user == null || !enrollmentService.isUserEnrolled(user.getId(), id)) {
            redirectAttributes.addFlashAttribute("reviewError", "Ban can dang ky khoa hoc truoc khi danh gia.");
            return "redirect:/courses/" + id;
        }

        Course course = courseService.findById(id);
        if (course == null) {
            return "redirect:/courses";
        }

        reviewService.saveOrUpdateReview(user, course, rating, comment);
        
        if (principal != null) {
            String requestedWith = ((HttpServletRequest) request).getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith)) {
                return ResponseEntity.ok("Review submitted successfully");
            }
        }

        redirectAttributes.addFlashAttribute("reviewSuccess", "Đã lưu bản đánh giá của bạn.");
        return "redirect:/courses/" + id + "#tab-reviews";
    }

    @PostMapping("/{id}/reviews/{reviewId}/helpful")
    @ResponseBody
    public ResponseEntity<?> toggleHelpful(@PathVariable Long id, @PathVariable Long reviewId, Principal principal) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            if (user != null) {
                reviewService.toggleHelpful(user, reviewId);
                
                // Return updated data for AJAX
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("helpfulCount", reviewService.getHelpfulCount(reviewId));
                response.put("isHelpful", reviewService.isHelpfulByUser(user.getId(), reviewId));
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(401).body("Unauthorized");
    }

    @PostMapping("/{id}/reviews/{reviewId}/report")
    public Object reportReview(@PathVariable Long id, @PathVariable Long reviewId,
                                @RequestParam("reason") String reason,
                                Principal principal, RedirectAttributes redirectAttributes,
                                HttpServletRequest request) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            if (user != null) {
                reviewService.reportReview(user, reviewId, reason);
                
                String requestedWith = request.getHeader("X-Requested-With");
                if ("XMLHttpRequest".equals(requestedWith)) {
                    return ResponseEntity.ok("Report submitted successfully");
                }
                
                redirectAttributes.addFlashAttribute("reviewSuccess", "Báo cáo thành công! Cảm ơn bạn đã phản hồi.");
            }
        }
        return "redirect:/courses/" + id + "#tab-reviews";
    }

    @GetMapping("/my-courses")
    public String myCourses(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        String username = principal.getName();

        User user = userService.findByUsername(username);
        if (user == null) {
            return "redirect:/auth/login";
        }

        List<Course> courses = enrollmentService.getCoursesByUserId(user.getId());

        model.addAttribute("courses", courses);
        model.addAttribute("user", user);

        return "course/my-courses";
    }

}
