package com.coursemanagementsystem.controller.admin;

import com.coursemanagementsystem.dto.CourseDTO;
import com.coursemanagementsystem.dto.LessonDTO;
import com.coursemanagementsystem.model.*;
import com.coursemanagementsystem.repository.CourseResourceRepository;
import com.coursemanagementsystem.repository.CourseSectionRepository;
import com.coursemanagementsystem.repository.LessonRepository;
import com.coursemanagementsystem.service.CourseService;
import com.coursemanagementsystem.service.FileService;
import com.coursemanagementsystem.service.LessonService;
import com.coursemanagementsystem.service.ReviewService;
import com.coursemanagementsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

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
    public String userList(Model model) {
        model.addAttribute("users", userService.findAllActiveUsers());
        model.addAttribute("activeMenu", "users");
        return "admin/user-list";
    }

    @GetMapping("/user-trash")
    public String userTrash(Model model) {
        model.addAttribute("deletedUsers", userService.findAllDeletedUsers());
        model.addAttribute("activeMenu", "user-trash");
        return "admin/user-trash";
    }

    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable("id") Long id,
                                 @RequestParam("roleName") String roleName,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Check if current user is admin
            User currentUser = principal == null ? null : userService.findByUsername(principal.getName());
            if (currentUser == null || !isAdminRole(currentUser)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện hành động này.");
                return "redirect:/admin/user-list";
            }

            userService.updateUserRole(id, roleName);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật vai trò thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/user-list";
    }

    private boolean isAdminRole(User user) {
        return user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());
    }

    @PostMapping("/users/{id}/delete")
    public String softDeleteUser(@PathVariable("id") Long id,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            User currentUser = principal == null ? null : userService.findByUsername(principal.getName());

            // Check if current user is admin
            if (currentUser == null || !isAdminRole(currentUser)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện hành động này.");
                return "redirect:/admin/user-list";
            }

            // Check if trying to delete self
            if (currentUser.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không thể tự xóa chính mình.");
                return "redirect:/admin/user-list";
            }

            userService.softDeleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã chuyển user vào thùng rác.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/user-list";
    }

    @PostMapping("/users/{id}/restore")
    public String restoreUser(@PathVariable("id") Long id,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            // Check if current user is admin
            User currentUser = principal == null ? null : userService.findByUsername(principal.getName());
            if (currentUser == null || !isAdminRole(currentUser)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện hành động này.");
                return "redirect:/admin/user-trash";
            }

            userService.restoreUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Khôi phục user thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/user-trash";
    }

    @GetMapping("/{id}")
    public String viewCourse(@PathVariable("id") Long id, Model model) {
        Course course = courseService.findByIdWithLessons(id);
        model.addAttribute("course", course);
        model.addAttribute("activeMenu", "courses");
        return "admin/admin-detail";
    }

    // --- REVIEW REPORTS MANAGEMENT ---

    @GetMapping("/reports")
    public String listReports(Model model) {
        model.addAttribute("reports", reviewService.getAllReports());
        model.addAttribute("activeMenu", "reports");
        return "admin/report-list";
    }

    @PostMapping("/reports/{id}/dismiss")
    public String dismissReport(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.dismissReport(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã bác bỏ báo cáo.");
        return "redirect:/admin/reports";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.deleteReview(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đánh giá và các báo cáo liên quan.");
        return "redirect:/admin/reports";
    }
}
