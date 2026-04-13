package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.UserRepository;
import com.coursemanagementsystem.service.CourseService;
import com.coursemanagementsystem.service.EnrollmentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseService courseService;
    private final UserRepository userRepository;

    public EnrollmentController(EnrollmentService enrollmentService,
                                CourseService courseService,
                                UserRepository userRepository) {
        this.enrollmentService = enrollmentService;
        this.courseService = courseService;
        this.userRepository = userRepository;
    }

    @GetMapping("/enroll/{courseId}")
    public String enroll(@PathVariable long courseId, Principal principal) {

        if (principal == null) {
            return "redirect:/auth/login";
        }

        String username = principal.getName();

        // xử lý Optional<User>
        Optional<User> optionalUser = userRepository.findByUserName(username);
        if (!optionalUser.isPresent()) {
            return "redirect:/auth/login";
        }
        User user = optionalUser.get();

        // Course bạn đang dùng kiểu thường
        Course course = courseService.findById(courseId);
        if (course == null) {
            return "redirect:/auth/courses";
        }

        enrollmentService.enroll(user, course);

        return "redirect:/courses";
    }
}