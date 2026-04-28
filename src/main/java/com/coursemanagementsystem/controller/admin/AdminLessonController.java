package com.coursemanagementsystem.controller.admin;

import com.coursemanagementsystem.dto.LessonDTO;
import com.coursemanagementsystem.model.Lesson;
import com.coursemanagementsystem.repository.CourseResourceRepository;
import com.coursemanagementsystem.repository.CourseSectionRepository;
import com.coursemanagementsystem.repository.LessonRepository;
import com.coursemanagementsystem.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminLessonController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private CourseSectionRepository courseSectionRepository;


    @GetMapping("/lesson-list")
    public String lessonList(@RequestParam(value = "page", defaultValue = "1") int page,
                             @RequestParam(value = "size", defaultValue = "6") int size,
                             @RequestParam(value = "keyword", defaultValue = "") String keyword,
                             Model model) {
        java.util.List<Lesson> allLessons = lessonService.searchLessons(keyword);
        int totalLessons = allLessons.size();
        int normalizedPage = Math.max(page - 1, 0);
        int totalPages = (int) Math.ceil((double) totalLessons / size);

        int start = normalizedPage * size;
        int end = Math.min(start + size, totalLessons);

        java.util.List<Lesson> paginatedLessons = start >= totalLessons ? java.util.List.of() : allLessons.subList(start, end);

        model.addAttribute("lessons", paginatedLessons);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalLessons);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
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
    @GetMapping("course/{courseId}/add-lesson")
    public String showAddLessonForm(@PathVariable("courseId") Long courseId,
                                    @RequestParam(value = "sectionId", required = false) Long sectionId,
                                    Model model) {
        LessonDTO dto = new LessonDTO();
        dto.setCourseId(courseId);
        dto.setSectionId(sectionId);
        model.addAttribute("lessonDTO", dto);
        model.addAttribute("courseId", courseId);
        model.addAttribute("sections", courseSectionRepository.findByCourseIdOrderByDisplayOrderAsc(courseId));
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
                model.addAttribute("sections", courseSectionRepository.findByCourseIdOrderByDisplayOrderAsc(dto.getCourseId()));
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
        dto.setDuration(lesson.getDuration());
        dto.setCourseId(lesson.getCourse().getId());
        dto.setSectionId(lesson.getSection() != null ? lesson.getSection().getId() : null);
        model.addAttribute("lessonDTO", dto);
        model.addAttribute("courseId", lesson.getCourse().getId());
        model.addAttribute("sections", courseSectionRepository.findByCourseIdOrderByDisplayOrderAsc(lesson.getCourse().getId()));
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
    @PostMapping("/delete-lesson-from-list/{id}")
    public String deleteLessonFromList(@PathVariable("id") Long id) {
        lessonService.deleteById(id);
        return "redirect:/admin/lesson-list";
    }

    @GetMapping("/create-lesson")
    public String createLesson(Model model) {
        model.addAttribute("lessonDTO", new LessonDTO());
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("activeMenu", "lessons");
        return "admin/add-lesson";
    }
}
