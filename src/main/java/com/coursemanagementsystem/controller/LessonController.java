package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.model.Lesson;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.EnrollmentService;
import com.coursemanagementsystem.service.LessonService;
import com.coursemanagementsystem.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LessonController {

    private final UserService userService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;

    public LessonController(UserService userService,
                            LessonService lessonService,
                            EnrollmentService enrollmentService) {
        this.userService = userService;
        this.lessonService = lessonService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/lessons/{id}")
    public String viewLesson(@PathVariable Long id, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
            return "redirect:/auth/login";
        }

        String username = auth.getName();

        User user = userService.findByUsername(username);

        if (user == null) {
            return "redirect:/auth/login";
        }

        Lesson lesson = lessonService.findById(id);

        if (lesson == null) {
            return "redirect:/courses";
        }

        Long courseId = lesson.getCourse().getId();

        boolean canViewWithoutEnrollment = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority) || "ROLE_INSTRUCTOR".equals(authority));

        boolean enrolled = canViewWithoutEnrollment || enrollmentService.isEnrolled(user.getId(), courseId);

        if (!enrolled) {
            return "redirect:/courses";
        }

        model.addAttribute("lesson", lesson);

        return "lesson/view";
    }
}