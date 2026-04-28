package com.coursemanagementsystem.controller.admin;

import com.coursemanagementsystem.model.*;
import com.coursemanagementsystem.service.CourseService;
import com.coursemanagementsystem.service.ReviewService;
import com.coursemanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private CourseService courseService;
    @Autowired
    private UserService userService;
    @Autowired
    private ReviewService reviewService;

    @GetMapping("/user-list")
    @PreAuthorize("hasRole('ADMIN')")
    public String userList(Model model) {
        model.addAttribute("users", userService.findAllActiveUsers());
        model.addAttribute("activeMenu", "users");
        return "admin/user/user-list";
    }

    @GetMapping("/user-trash")
    @PreAuthorize("hasRole('ADMIN')")
    public String userTrash(Model model) {
        model.addAttribute("deletedUsers", userService.findAllDeletedUsers());
        model.addAttribute("activeMenu", "user-trash");
        return "admin/user/user-trash";
    }

    @PostMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUserRole(@PathVariable("id") Long id,
                                 @RequestParam("roleName") String roleName,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserRole(id, roleName);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật vai trò thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/user/user-list";
    }

    @PostMapping("/users/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String softDeleteUser(@PathVariable("id") Long id,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            User currentUser = principal == null ? null : userService.findByUsername(principal.getName());

            // Check if trying to delete self
            if (currentUser != null && currentUser.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không thể tự xóa chính mình.");
                return "redirect:/admin/user/user-list";
            }

            userService.softDeleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã chuyển user vào thùng rác.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/user/user-list";
    }

    @PostMapping("/users/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public String restoreUser(@PathVariable("id") Long id,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.restoreUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Khôi phục user thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/user/user-trash";
    }

    // --- REVIEW REPORTS MANAGEMENT ---

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public String listReports(Model model) {
        model.addAttribute("reports", reviewService.getAllReports());
        model.addAttribute("activeMenu", "reports");
        return "admin/report-list";
    }

    @PostMapping("/reports/{id}/dismiss")
    @PreAuthorize("hasRole('ADMIN')")
    public String dismissReport(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.dismissReport(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã bác bỏ báo cáo.");
        return "redirect:/admin/reports";
    }

    @PostMapping("/reviews/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.deleteReview(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đánh giá và các báo cáo liên quan.");
        return "redirect:/admin/reports";
    }
}
