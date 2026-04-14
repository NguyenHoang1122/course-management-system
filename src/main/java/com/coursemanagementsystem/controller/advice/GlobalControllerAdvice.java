package com.coursemanagementsystem.controller.advice;

import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserService userService;

    @ModelAttribute
    public void addUserInfoToModel(Model model, Principal principal) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("username", user.getUserName());
                model.addAttribute("avatarUrl", user.getAvatar());
                model.addAttribute("fullName", user.getFullName());
            }
        }
    }
}
