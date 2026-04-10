package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.dto.CourseDTO;
import com.coursemanagementsystem.dto.LessonDTO;
import com.coursemanagementsystem.model.Lesson;
import com.coursemanagementsystem.service.CourseService;
import com.coursemanagementsystem.service.LessonService;
import com.coursemanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private CourseService courseService;
    @Autowired
    private UserService userService;
    @Autowired
    private LessonService lessonService;

    @GetMapping("/course-list.html")
    public String courseList(Model model) {
        model.addAttribute("courses", courseService.findAll());
        return "admin/course-list";
    }

    @GetMapping("/create-course")
    public String createCourse(Model model) {
        model.addAttribute("courseDTO", new CourseDTO());
        model.addAttribute("instructors", userService.findAllInstructor());
        return "admin/create-course";
    }

    @PostMapping("/save-course")
    public String saveCourse(@ModelAttribute("courseDTO") CourseDTO courseDTO) {
        courseService.saveFromDTO(courseDTO);
        return "redirect:/courses";
    }

    @GetMapping("course/{courseId}/add-lesson")
    public String showAddLessonForm(@PathVariable("courseId") Long courseId, Model model) {
        LessonDTO dto = new LessonDTO();
        dto.setCourseId(courseId);
        model.addAttribute("lessonDTO", dto);
        model.addAttribute("courseId", courseId);
        model.addAttribute("courseTitle", courseService.findById(courseId).getTitle());
        return "admin/add-lesson";
    }

    @PostMapping("/save-lesson")
    public String saveLesson(@ModelAttribute LessonDTO dto) {
        lessonService.saveFromDTO(dto);
        return "redirect:/courses/" + dto.getCourseId();
    }

    @GetMapping("/edit/{id}")
    public String editCourse(@PathVariable("id") Long id, Model model) {
        model.addAttribute("courseDTO", courseService.findDTOById(id));
        model.addAttribute("instructors", userService.findAllInstructor());
        return "admin/edit-course";
    }

    @PostMapping("/update")
    public String updateCourse(@ModelAttribute("courseDTO") CourseDTO courseDTO) {
        courseService.saveFromDTO(courseDTO);
        return "redirect:/courses";
    }

    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id) {
        courseService.deleteById(id);
        return "redirect:/courses";
    }

    @GetMapping("/edit-lesson/{id}")
    public String editLesson(@PathVariable("id") Long id, Model model) {
        Lesson lesson = lessonService.findById(id);
        if (lesson == null) {
            return "redirect:/courses";
        }
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setVideoUrl(lesson.getVideoUrl());
        dto.setCourseId(lesson.getCourse().getId());
        model.addAttribute("lessonDTO", dto);
        model.addAttribute("courseId", lesson.getCourse().getId());
        model.addAttribute("courseTitle", lesson.getCourse().getTitle());
        return "admin/add-lesson"; // reuse the same template
    }

    @PostMapping("/update-lesson")
    public String updateLesson(@ModelAttribute LessonDTO dto) {
        lessonService.saveFromDTO(dto);
        return "redirect:/courses/" + dto.getCourseId();
    }

    @PostMapping("/delete-lesson/{id}")
    public String deleteLesson(@PathVariable("id") Long id) {
        Lesson lesson = lessonService.findById(id);
        if (lesson != null) {
            Long courseId = lesson.getCourse().getId();
            lessonService.deleteById(id);
            return "redirect:/courses/" + courseId;
        }
        return "redirect:/courses";
    }

}
