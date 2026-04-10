package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.model.Enrollment;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.EnrollmentService;
import com.coursemanagementsystem.service.FileService;
import com.coursemanagementsystem.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Controller
public class ProfileController {

    private final UserService userService;
    private final FileService fileService;
    private final EnrollmentService enrollmentService;

    public ProfileController(UserService userService, FileService fileService, EnrollmentService enrollmentService) {
        this.userService = userService;
        this.fileService = fileService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        String username = principal.getName();
        User user = userService.findByUsername(username);

        List<Enrollment> enrollments = enrollmentService.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("enrollments", enrollments);

        return "profile/view";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        String username = principal.getName();
        User user = userService.findByUsername(username);

        model.addAttribute("user", user);

        return "profile/edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@ModelAttribute("user") User userForm,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                Principal principal,
                                Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        String username = principal.getName();

        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                userForm.setAvatar(fileService.uploadFile(avatarFile));
            } catch (Exception ex) {
                model.addAttribute("user", currentUser);
                model.addAttribute("updateError", ex.getMessage());
                return "profile/edit";
            }
        } else {
            userForm.setAvatar(currentUser.getAvatar());
        }

        if (userForm.getFullName() == null || userForm.getFullName().trim().isEmpty()) {
            model.addAttribute("user", currentUser);
            model.addAttribute("updateError", "Full name is required");
            return "profile/edit";
        }

        try {
            userService.updateProfile(username, userForm);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("user", currentUser);
            model.addAttribute("updateError", ex.getMessage());
            return "profile/edit";
        }

        return "redirect:/profile?updated=true";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Principal principal,
                                 Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        String username = principal.getName();
        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        try {
            userService.changePassword(username, currentPassword, newPassword, confirmPassword);
            return "redirect:/profile/edit?passwordUpdated=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("user", currentUser);
            model.addAttribute("changePasswordError", ex.getMessage());
            return "profile/edit";
        }
    }
}