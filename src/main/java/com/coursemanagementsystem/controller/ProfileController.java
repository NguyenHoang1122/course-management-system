package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.dto.UserProfileDTO;
import com.coursemanagementsystem.model.Enrollment;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.EnrollmentService;
import com.coursemanagementsystem.service.FileService;
import com.coursemanagementsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final UserDetailsService userDetailsService;

    public ProfileController(UserService userService,
                             FileService fileService,
                             EnrollmentService enrollmentService,
                             UserDetailsService userDetailsService) {
        this.userService = userService;
        this.fileService = fileService;
        this.enrollmentService = enrollmentService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/login";

        User user = userService.findByUsername(principal.getName());
        List<Enrollment> enrollments = enrollmentService.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("enrollments", enrollments);
        return "profile/view";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/login";

        User user = userService.findByUsername(principal.getName());

        model.addAttribute("user", user);
        model.addAttribute("userProfileDTO", buildProfileDTO(user));
        return "profile/edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute("userProfileDTO") UserProfileDTO profileDTO,
                                BindingResult bindingResult,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                Principal principal,
                                Model model) {
        if (principal == null) return "redirect:/auth/login";

        String username = principal.getName();
        User currentUser = userService.findByUsername(username);
        if (currentUser == null) return "redirect:/auth/login";

        // Xử lý upload avatar
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                profileDTO.setAvatar(fileService.uploadFile(avatarFile));
            } catch (Exception ex) {
                model.addAttribute("user", currentUser);
                model.addAttribute("updateError", ex.getMessage());
                return "profile/edit";
            }
        } else {
            profileDTO.setAvatar(currentUser.getAvatar());
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", currentUser);
            return "profile/edit";
        }

        try {
            userService.updateProfile(username, profileDTO);
            // Làm mới SecurityContext ngay sau khi update
            // để avatar/fullName trên header cập nhật luôn mà không cần logout
            refreshSecurityContext(username);
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
        if (principal == null) return "redirect:/auth/login";

        String username = principal.getName();
        User currentUser = userService.findByUsername(username);
        if (currentUser == null) return "redirect:/auth/login";

        try {
            userService.changePassword(username, currentPassword, newPassword, confirmPassword);
            return "redirect:/profile/edit?passwordUpdated=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("user", currentUser);
            model.addAttribute("userProfileDTO", buildProfileDTO(currentUser));
            model.addAttribute("changePasswordError", ex.getMessage());
            return "profile/edit";
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /** Tải lại UserDetails từ DB và cập nhật Authentication trong SecurityContext của session hiện tại */
    private void refreshSecurityContext(String username) {
        UserDetails updatedDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(
                        updatedDetails,
                        updatedDetails.getPassword(),
                        updatedDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    private UserProfileDTO buildProfileDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        return dto;
    }
}

