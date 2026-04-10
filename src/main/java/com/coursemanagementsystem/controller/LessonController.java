package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.model.Lesson;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.EnrollmentService;
import com.coursemanagementsystem.service.LessonService;
import com.coursemanagementsystem.service.UserService;
import org.springframework.security.core.Authentication;
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
    public String viewLesson(@PathVariable Long id, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = auth.getName();

        User user = userService.findByUsername(username);

        Lesson lesson = lessonService.getLessonForUser(id, user.getId());

        model.addAttribute("lesson", lesson);

        return "lesson/detail";
    }
}