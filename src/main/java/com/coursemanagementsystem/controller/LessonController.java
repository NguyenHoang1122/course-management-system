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

    @GetMapping("/lesson/{id}")
    public String viewLesson(@PathVariable Long id, Model model, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username);

        // Lấy Lesson gốc không qua kiểm tra quyền tĩnh
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

        if (lesson.getVideoUrl() != null && !lesson.getVideoUrl().isEmpty()) {
            return "redirect:" + lesson.getVideoUrl();
        }

        // Return to course detailing page if no video available
        redirectAttributes.addFlashAttribute("error", "Bài học này chưa có video!");
        return "redirect:/courses/" + lesson.getCourse().getId();
    }
}