package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.dto.UserDTO;
import com.coursemanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String profile(Model model) {
        UserDTO userDTO = userService.getProfile(1L); // tạm hardcode

        model.addAttribute("user", userDTO);

        return "user/profile";
    }
}
