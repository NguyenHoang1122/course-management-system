package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.model.Lesson;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.EnrollmentService;
import com.coursemanagementsystem.service.LessonProgressService;
import com.coursemanagementsystem.service.LessonService;
import com.coursemanagementsystem.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LessonController {

    private final UserService userService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final LessonProgressService lessonProgressService;

    public LessonController(UserService userService,
                            LessonService lessonService,
                            EnrollmentService enrollmentService,
                            LessonProgressService lessonProgressService) {
        this.userService = userService;
        this.lessonService = lessonService;
        this.enrollmentService = enrollmentService;
        this.lessonProgressService = lessonProgressService;
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

        model.addAttribute("lesson", lesson);
        model.addAttribute("isCompleted", lessonProgressService.isCompleted(user.getId(), lesson.getId()));

        return "lesson/view";
    }

    @PostMapping("/lessons/{id}/complete")
    public String markLessonCompleted(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
            return "redirect:/auth/login";
        }

        User user = userService.findByUsername(auth.getName());
        Lesson lesson = lessonService.findById(id);

        if (user == null || lesson == null) {
            return "redirect:/courses";
        }

        Long courseId = lesson.getCourse().getId();
        boolean canViewWithoutEnrollment = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority) || "ROLE_INSTRUCTOR".equals(authority));

        if (!canViewWithoutEnrollment && !enrollmentService.isEnrolled(user.getId(), courseId)) {
            return "redirect:/courses";
        }

        lessonProgressService.markCompleted(user, lesson);
        redirectAttributes.addFlashAttribute("lessonSuccess", "Da danh dau bai hoc la da hoc.");
        return "redirect:/lessons/" + id;
    }

    @PostMapping("/lessons/{id}/toggle-progress")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<String> toggleLessonProgress(@PathVariable Long id) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
            return org.springframework.http.ResponseEntity.status(401).body("UNAUTHORIZED");
        }

        User user = userService.findByUsername(auth.getName());
        Lesson lesson = lessonService.findById(id);

        if (user == null || lesson == null) {
            return org.springframework.http.ResponseEntity.status(404).body("NOT_FOUND");
        }

        boolean canToggle = auth.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority) || "ROLE_INSTRUCTOR".equals(authority))
                || enrollmentService.isEnrolled(user.getId(), lesson.getCourse().getId());

        if (!canToggle) {
            return org.springframework.http.ResponseEntity.status(403).body("FORBIDDEN");
        }

        boolean isCompletedNow = lessonProgressService.toggleProgress(user, lesson);
        return org.springframework.http.ResponseEntity.ok(isCompletedNow ? "COMPLETED" : "REMOVED");
    }
}