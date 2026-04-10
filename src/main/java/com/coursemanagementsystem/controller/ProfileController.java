package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.dto.UserProfileDTO;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.FileService;
import com.coursemanagementsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
public class ProfileController {

    private final UserService userService;
    private final FileService fileService;

    public ProfileController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        String username = principal.getName();
        User user = userService.findByUsername(username);

        model.addAttribute("user", user);

        return "profile/view";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        String username = principal.getName();
        User user = userService.findByUsername(username);

        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setFullName(user.getFullName());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setPhone(user.getPhone());
        profileDTO.setAddress(user.getAddress());

        model.addAttribute("user", user);
        model.addAttribute("userProfileDTO", profileDTO);
        return "profile/edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute("userProfileDTO") UserProfileDTO profileDTO,
                                BindingResult bindingResult,
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
            model.addAttribute("userProfileDTO", buildProfileDTO(currentUser));
            model.addAttribute("changePasswordError", ex.getMessage());
            return "profile/edit";
        }
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