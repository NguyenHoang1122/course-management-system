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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String viewCourse(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseService.findByIdWithLessons(id);
        if (course == null) {
            return "redirect:/courses";
        }

        boolean enrolled = false;
        Set<Long> completedLessonIds = Collections.emptySet();
        Review userReview = null;

        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            if (user != null) {
                enrolled = enrollmentService.isUserEnrolled(user.getId(), id);
                completedLessonIds = lessonProgressService.getCompletedLessonIds(user.getId(), id);
                Optional<Review> currentReview = reviewService.getUserReview(user.getId(), id);
                userReview = currentReview.orElse(null);
                model.addAttribute("isWishlisted", wishlistService.isWishlisted(user, id));
            }
        } else {
            model.addAttribute("isWishlisted", false);
        }

        model.addAttribute("course", course);
        model.addAttribute("isEnrolled", enrolled);
        model.addAttribute("completedLessonIds", completedLessonIds);
        model.addAttribute("averageRating", reviewService.getAverageRating(id));
        model.addAttribute("reviews", reviewService.getReviewsByCourseId(id));
        model.addAttribute("userReview", userReview);
        model.addAttribute("studentCount", enrollmentService.countEnrollmentsByCourseId(id));
        return "course/detail";
    }

    @PostMapping("/{id}/reviews")
    public String submitReview(@PathVariable Long id,
                               @RequestParam("rating") Integer rating,
                               @RequestParam(value = "comment", required = false) String comment,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
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
        redirectAttributes.addFlashAttribute("reviewSuccess", "Da luu danh gia cua ban.");
        return "redirect:/courses/" + id;
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
