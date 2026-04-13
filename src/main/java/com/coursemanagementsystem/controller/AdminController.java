package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.dto.CourseDTO;
import com.coursemanagementsystem.dto.LessonDTO;
import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.Lesson;
import com.coursemanagementsystem.service.CourseService;
import com.coursemanagementsystem.service.LessonService;
import com.coursemanagementsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    @GetMapping("/course-list")
    public String findALlCourseList(@RequestParam(value = "page", defaultValue = "1") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                    @RequestParam(value = "keyword", defaultValue = "") String keyword,
                                    Model model) {
        Page<Course> coursePage = courseService.findCoursesPaged(keyword, page, size);

        // Calculate total value
        long totalValue = coursePage.getContent().stream()
                .mapToLong(course -> course.getPrice() != null ? course.getPrice().longValue() : 0)
                .sum();

        // Calculate total lessons
        long totalLessons = coursePage.getContent().stream()
                .mapToLong(course -> course.getLessons() != null ? course.getLessons().size() : 0)
                .sum();

        model.addAttribute("coursePage", coursePage);
        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("totalItems", coursePage.getTotalElements());
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("totalLessons", totalLessons);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "courses");
        return "admin/course-list";
    }

    @GetMapping("/create-course")
    public String createCourse(Model model) {
        model.addAttribute("courseDTO", new CourseDTO());
        model.addAttribute("instructors", userService.findAllInstructor());
        model.addAttribute("activeMenu", "courses");
        return "admin/create-course";
    }

    @PostMapping("/save-course")
    public String saveCourse(@Valid @ModelAttribute("courseDTO") CourseDTO courseDTO,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("instructors", userService.findAllInstructor());
            model.addAttribute("activeMenu", "courses");
            return "admin/create-course";
        }
        courseService.saveFromDTO(courseDTO);
        return "redirect:/admin/course-list";
    }

    @GetMapping("course/{courseId}/add-lesson")
    public String showAddLessonForm(@PathVariable("courseId") Long courseId, Model model) {
        LessonDTO dto = new LessonDTO();
        dto.setCourseId(courseId);
        model.addAttribute("lessonDTO", dto);
        model.addAttribute("courseId", courseId);
        model.addAttribute("courseTitle", courseService.findById(courseId).getTitle());
        model.addAttribute("activeMenu", "lessons");
        return "admin/add-lesson";
    }

    @PostMapping("/save-lesson")
    public String saveLesson(@Valid @ModelAttribute("lessonDTO") LessonDTO dto,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("courseId", dto.getCourseId());
            if (dto.getCourseId() != null) {
                model.addAttribute("courseTitle", courseService.findById(dto.getCourseId()).getTitle());
            } else {
                model.addAttribute("courses", courseService.findAll());
            }
            model.addAttribute("activeMenu", "lessons");
            return "admin/add-lesson";
        }
        lessonService.saveFromDTO(dto);
        if (dto.getCourseId() != null) {
            return "redirect:/admin/" + dto.getCourseId();
        }
        return "redirect:/admin/lesson-list";
    }

    @GetMapping("/edit/{id}")
    public String editCourse(@PathVariable("id") Long id, Model model) {
        model.addAttribute("courseDTO", courseService.findDTOById(id));
        model.addAttribute("instructors", userService.findAllInstructor());
        model.addAttribute("activeMenu", "courses");
        return "admin/edit-course";
    }

    @PostMapping("/update")
    public String updateCourse(@Valid @ModelAttribute("courseDTO") CourseDTO courseDTO,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("instructors", userService.findAllInstructor());
            model.addAttribute("activeMenu", "courses");
            return "admin/edit-course";
        }
        courseService.saveFromDTO(courseDTO);
        return "redirect:/admin/course-list";
    }

    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id) {
        courseService.deleteById(id);
        return "redirect:/admin/course-list";
    }

    @GetMapping("/edit-lesson/{id}")
    public String editLesson(@PathVariable("id") Long id, Model model) {
        Lesson lesson = lessonService.findById(id);
        if (lesson == null) {
            return "redirect:/admin/lesson-list";
        }
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setVideoUrl(lesson.getVideoUrl());
        dto.setCourseId(lesson.getCourse().getId());
        model.addAttribute("lessonDTO", dto);
        model.addAttribute("courseId", lesson.getCourse().getId());
        model.addAttribute("courseTitle", lesson.getCourse().getTitle());
        model.addAttribute("activeMenu", "lessons");
        return "admin/add-lesson"; // reuse the same template
    }

    @PostMapping("/update-lesson")
    public String updateLesson(@Valid @ModelAttribute("lessonDTO") LessonDTO dto,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("courseId", dto.getCourseId());
            model.addAttribute("courseTitle", courseService.findById(dto.getCourseId()).getTitle());
            model.addAttribute("activeMenu", "lessons");
            return "admin/add-lesson";
        }
        lessonService.saveFromDTO(dto);
        return "redirect:/admin/" + dto.getCourseId();
    }

    @PostMapping("/delete-lesson/{id}")
    public String deleteLesson(@PathVariable("id") Long id) {
        Lesson lesson = lessonService.findById(id);
        if (lesson != null) {
            Long courseId = lesson.getCourse().getId();
            lessonService.deleteById(id);
            return "redirect:/admin/" + courseId;
        }
        return "redirect:/admin/course-list";
    }

    @PostMapping("/delete-lesson-from-list/{id}")
    public String deleteLessonFromList(@PathVariable("id") Long id) {
        lessonService.deleteById(id);
        return "redirect:/admin/lesson-list";
    }

    @GetMapping("/{id}")
    public String viewCourse(@PathVariable("id") Long id, Model model) {
        Course course = courseService.findByIdWithLessons(id);
        model.addAttribute("course", course);
        model.addAttribute("activeMenu", "courses");
        return "admin/admin-detail";
    }

    @GetMapping("/lesson-list")
    public String lessonList(Model model) {
        model.addAttribute("lessons", lessonService.findAll());
        model.addAttribute("activeMenu", "lessons");
        return "admin/lesson-list";
    }

    @GetMapping("/lesson-detail/{id}")
    public String lessonDetail(@PathVariable("id") Long id, Model model) {
        Lesson lesson = lessonService.findById(id);
        if (lesson == null) {
            return "redirect:/admin/lesson-list";
        }
        model.addAttribute("lesson", lesson);
        model.addAttribute("activeMenu", "lessons");
        return "admin/lesson-detail";
    }

    @GetMapping("/create-lesson")
    public String createLesson(Model model) {
        model.addAttribute("lessonDTO", new LessonDTO());
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("activeMenu", "lessons");
        return "admin/add-lesson";
    }
}
