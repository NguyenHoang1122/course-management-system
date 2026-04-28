package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.model.CourseSection;
import com.coursemanagementsystem.service.CourseSectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sections")
public class CourseSectionController {

    @Autowired
    private CourseSectionService courseSectionService;

    @PostMapping("/add")
    public String addSection(@RequestParam("courseId") Long courseId,
                             @RequestParam("title") String title,
                             RedirectAttributes redirectAttributes) {
        try {
            courseSectionService.addSection(courseId, title);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm chương mới thành công.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/" + courseId;
    }

    @PostMapping("/update")
    public String updateSection(@RequestParam("id") Long id,
                                @RequestParam("title") String title,
                                RedirectAttributes redirectAttributes) {
        CourseSection section = courseSectionService.updateSection(id, title);
        if (section != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật tên chương thành công.");
            return "redirect:/admin/" + section.getCourse().getId();
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Chương không tồn tại.");
        return "redirect:/admin/course-list";
    }

    @PostMapping("/delete/{id}")
    public String deleteSection(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        CourseSection section = courseSectionService.findById(id).orElse(null);
        if (section != null) {
            Long courseId = section.getCourse().getId();
            if (courseSectionService.deleteSection(id)) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã xóa chương thành công.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa chương.");
            }
            return "redirect:/admin/" + courseId;
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Chương không tồn tại.");
        return "redirect:/admin/course-list";
    }
}
