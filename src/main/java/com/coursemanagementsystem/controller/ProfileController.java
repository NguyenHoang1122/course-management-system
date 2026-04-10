package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userService.findByUsername(username);

        model.addAttribute("user", user);

        return "profile/view";
    }
    //form edit
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

    //update profile
    @PostMapping("/profile/edit")
    public  String updateProfile(User userForm, Principal principal) {
        String username = principal.getName();

        userService.updateProfile(username, userForm);

        return "redirect:/profile";
    }
}
