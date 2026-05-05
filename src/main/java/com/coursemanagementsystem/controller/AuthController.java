package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.dto.UserRegisterDTO;
import jakarta.validation.Valid;
import com.coursemanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("userRegisterDTO", new UserRegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("userRegisterDTO") UserRegisterDTO dto,
                                  BindingResult bindingResult,
                                  Model model) {

        // Kiểm tra mật khẩu xác nhận khớp (cross-field validation)
        if (!dto.getPassword().isBlank() && !dto.getConfirmPassword().isBlank()
                && !dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
                    "Mật khẩu xác nhận không khớp với mật khẩu đã nhập");
        }

        // Trả về form nếu có lỗi validation
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(dto);
            return "redirect:/auth/login?register=true";
        } catch (IllegalArgumentException ex) {
            // Xử lý lỗi business logic: username/email đã tồn tại
            String message = ex.getMessage();
            if (message != null && message.contains("Username")) {
                bindingResult.rejectValue("userName", "error.userName",
                        "Tên đăng nhập này đã được sử dụng, vui lòng chọn tên khác");
            } else if (message != null && message.contains("Email")) {
                bindingResult.rejectValue("email", "error.email",
                        "Email này đã được đăng ký, vui lòng dùng email khác");
            } else {
                model.addAttribute("registerError", message);
            }
            return "auth/register";
        }
    }
}