package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.CourseSection;
import com.coursemanagementsystem.repository.CourseSectionRepository;
import com.coursemanagementsystem.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sections")
public class CourseSectionController {

    @Autowired
    private CourseSectionRepository courseSectionRepository;

    @Autowired
    private CourseService courseService;

    @PostMapping("/add")
    public String addSection(@RequestParam("courseId") Long courseId,
                             @RequestParam("title") String title,
                             RedirectAttributes redirectAttributes) {
        Course course = courseService.findById(courseId);
        if (course != null) {
            CourseSection section = new CourseSection();
            section.setTitle(title);
            section.setCourse(course);
            // Get current max order
            int maxOrder = course.getSections().size();
            section.setDisplayOrder(maxOrder + 1);
            
            courseSectionRepository.save(section);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm chương mới thành công.");
        }
        return "redirect:/admin/" + courseId;
    }

    @PostMapping("/update")
    public String updateSection(@RequestParam("id") Long id,
                                @RequestParam("title") String title,
                                RedirectAttributes redirectAttributes) {
        CourseSection section = courseSectionRepository.findById(id).orElse(null);
        if (section != null) {
            section.setTitle(title);
            courseSectionRepository.save(section);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật tên chương thành công.");
            return "redirect:/admin/" + section.getCourse().getId();
        }
        return "redirect:/admin/course-list";
    }

    @PostMapping("/delete/{id}")
    public String deleteSection(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        CourseSection section = courseSectionRepository.findById(id).orElse(null);
        if (section != null) {
            Long courseId = section.getCourse().getId();
            courseSectionRepository.delete(section);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa chương thành công.");
            return "redirect:/admin/" + courseId;
        }
        return "redirect:/admin/course-list";
    }
}
