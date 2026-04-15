package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.dto.UserProfileDTO;
import com.coursemanagementsystem.dto.EnrollmentCourseProgressDTO;
import com.coursemanagementsystem.model.Enrollment;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.EnrollmentService;
import com.coursemanagementsystem.service.FileService;
import com.coursemanagementsystem.service.LessonProgressService;
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
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProfileController {

    private final UserService userService;
    private final FileService fileService;
    private final EnrollmentService enrollmentService;
    private final UserDetailsService userDetailsService;
    private final LessonProgressService lessonProgressService;

    public ProfileController(UserService userService,
                             FileService fileService,
                             EnrollmentService enrollmentService,
                             UserDetailsService userDetailsService,
                             LessonProgressService lessonProgressService) {
        this.userService = userService;
        this.fileService = fileService;
        this.enrollmentService = enrollmentService;
        this.userDetailsService = userDetailsService;
        this.lessonProgressService = lessonProgressService;
    }

    // ──────────────────────────────────────────────────────────────
    // ─────── USER PROFILE ENDPOINTS ────────────────────────────────
    // ──────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/login";

        User user = userService.findByUsername(principal.getName());
        
        // Check if user is admin
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            return "redirect:/admin/profile";
        }
        
        List<Enrollment> enrollments = enrollmentService.findByUserId(user.getId());
        List<EnrollmentCourseProgressDTO> enrollmentProgress = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            long totalLessons = enrollment.getCourse() != null && enrollment.getCourse().getLessons() != null
                    ? enrollment.getCourse().getLessons().size()
                    : 0;
            long completedLessons = enrollment.getCourse() == null
                    ? 0
                    : lessonProgressService.countCompletedLessons(user.getId(), enrollment.getCourse().getId());
            int progressPercent = totalLessons == 0 ? 0 : (int) ((completedLessons * 100) / totalLessons);
            String learningStatus = progressPercent == 100 ? "Hoan thanh" : (progressPercent > 0 ? "Dang hoc" : "Chua bat dau");

            enrollmentProgress.add(new EnrollmentCourseProgressDTO(
                    enrollment,
                    completedLessons,
                    totalLessons,
                    progressPercent,
                    learningStatus
            ));
        }

        model.addAttribute("user", user);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("enrollmentProgress", enrollmentProgress);
        return "profile/view";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/login";

        User user = userService.findByUsername(principal.getName());
        
        // Check if user is admin
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            return "redirect:/admin/profile/edit";
        }

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

        // Handle avatar upload
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

    // ──────────────────────────────────────────────────────────────
    // ─────── ADMIN PROFILE ENDPOINTS ────────────────────────────────
    // ──────────────────────────────────────────────────────────────

    @GetMapping("/admin/profile")
    public String adminViewProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/login";

        User user = userService.findByUsername(principal.getName());
        
        // Check if user is NOT admin
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            return "redirect:/profile";
        }

        model.addAttribute("user", user);
        model.addAttribute("activeMenu", "profile");
        return "admin/admin-view";
    }

    @GetMapping("/admin/profile/edit")
    public String adminEditProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/login";

        User user = userService.findByUsername(principal.getName());
        
        // Check if user is NOT admin
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            return "redirect:/profile/edit";
        }

        model.addAttribute("user", user);
        model.addAttribute("userProfileDTO", buildProfileDTO(user));
        model.addAttribute("activeMenu", "profile");
        return "admin/admin-edit";
    }

    @PostMapping("/admin/profile/edit")
    public String adminUpdateProfile(@Valid @ModelAttribute("userProfileDTO") UserProfileDTO profileDTO,
                                     BindingResult bindingResult,
                                     @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                     Principal principal,
                                     Model model) {
        if (principal == null) return "redirect:/auth/login";

        String username = principal.getName();
        User currentUser = userService.findByUsername(username);
        
        // Check if user is NOT admin
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            return "redirect:/profile";
        }

        // Handle avatar upload
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                profileDTO.setAvatar(fileService.uploadFile(avatarFile));
            } catch (Exception ex) {
                model.addAttribute("user", currentUser);
                model.addAttribute("updateError", ex.getMessage());
                model.addAttribute("activeMenu", "profile");
                return "admin/admin-edit";
            }
        } else {
            profileDTO.setAvatar(currentUser.getAvatar());
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", currentUser);
            model.addAttribute("activeMenu", "profile");
            return "admin/admin-edit";
        }

        try {
            userService.updateProfile(username, profileDTO);
            refreshSecurityContext(username);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("user", currentUser);
            model.addAttribute("updateError", ex.getMessage());
            model.addAttribute("activeMenu", "profile");
            return "admin/admin-edit";
        }

        return "redirect:/admin/profile?updated=true";
    }

    @PostMapping("/admin/profile/change-password")
    public String adminChangePassword(@RequestParam("currentPassword") String currentPassword,
                                      @RequestParam("newPassword") String newPassword,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      Principal principal,
                                      Model model) {
        if (principal == null) return "redirect:/auth/login";

        String username = principal.getName();
        User currentUser = userService.findByUsername(username);
        
        // Check if user is NOT admin
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            return "redirect:/profile";
        }

        try {
            userService.changePassword(username, currentPassword, newPassword, confirmPassword);
            return "redirect:/admin/profile/edit?passwordUpdated=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("user", currentUser);
            model.addAttribute("userProfileDTO", buildProfileDTO(currentUser));
            model.addAttribute("changePasswordError", ex.getMessage());
            model.addAttribute("activeMenu", "profile");
            return "admin/admin-edit";
        }
    }

    // ──────────────────────────────────────────────────────────────
    // ─────── HELPER METHODS ────────────────────────────────────────
    // ──────────────────────────────────────────────────────────────

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
