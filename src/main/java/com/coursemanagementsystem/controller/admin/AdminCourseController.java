package com.coursemanagementsystem.controller.admin;

import com.coursemanagementsystem.dto.CourseDTO;
import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.CourseSection;
import com.coursemanagementsystem.model.Lesson;
import com.coursemanagementsystem.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminCourseController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private UserService userService;
    @Autowired
    private LessonService lessonService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private CourseSectionService courseSectionService;
    @Autowired
    private CourseResourceService courseResourceService;

    @GetMapping("/course-list")
    public String findALlCourseList(@RequestParam(value = "page", defaultValue = "1") int page,
                                    @RequestParam(value = "size", defaultValue = "6") int size,
                                    @RequestParam(value = "keyword", defaultValue = "") String keyword,
                                    Model model) {
        java.util.List<Course> allCourses = courseService.searchCoursesByKeyword(keyword);
        int totalCourses = allCourses.size();
        int normalizedPage = Math.max(page - 1, 0);
        int totalPages = (int) Math.ceil((double) totalCourses / size);

        int start = normalizedPage * size;
        int end = Math.min(start + size, totalCourses);

        java.util.List<Course> paginatedCourses = start >= totalCourses ? java.util.List.of() : allCourses.subList(start, end);

        // Calculate total value
        long totalValue = paginatedCourses.stream()
                .mapToLong(course -> course.getPrice() != null ? course.getPrice().longValue() : 0)
                .sum();

        // Calculate total lessons
        long totalLessons = paginatedCourses.stream()
                .mapToLong(course -> course.getLessons() != null ? course.getLessons().size() : 0)
                .sum();

        // Calculate average ratings
        Map<Long, Double> averageRatings = new HashMap<>();
        for (Course course : paginatedCourses) {
            averageRatings.put(course.getId(), reviewService.getAverageRating(course.getId()));
        }

        model.addAttribute("courses", paginatedCourses);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalCourses);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("totalLessons", totalLessons);
        model.addAttribute("averageRatings", averageRatings);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "courses");
        return "admin/course/course-list";
    }

    @GetMapping("/create-course")
    public String createCourse(Model model) {
        model.addAttribute("courseDTO", new CourseDTO());
        model.addAttribute("instructors", userService.findAllInstructor());
        model.addAttribute("activeMenu", "courses");
        return "admin/course/create-course";
    }

    @PostMapping("/save-course")
    public String saveCourse(@Valid @ModelAttribute("courseDTO") CourseDTO courseDTO,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("instructors", userService.findAllInstructor());
            model.addAttribute("activeMenu", "courses");
            return "admin/course/create-course";
        }
        courseService.saveFromDTO(courseDTO);
        return "redirect:/admin/course/course-list";
    }

    @GetMapping("/edit/{id}")
    public String editCourse(@PathVariable("id") Long id, Model model) {
        model.addAttribute("courseDTO", courseService.findDTOById(id));
        model.addAttribute("instructors", userService.findAllInstructor());
        model.addAttribute("activeMenu", "courses");
        return "admin/course/edit-course";
    }

    @PostMapping("/update")
    public String updateCourse(@Valid @ModelAttribute("courseDTO") CourseDTO courseDTO,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("instructors", userService.findAllInstructor());
            model.addAttribute("activeMenu", "courses");
            return "admin/course/edit-course";
        }
        courseService.saveFromDTO(courseDTO);
        return "redirect:/admin/course/course-list";
    }

    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id) {
        courseService.deleteById(id);
        return "redirect:/admin/course/course-list";
    }

    @PostMapping("/delete-lesson/{id}")
    public String deleteLesson(@PathVariable("id") Long id) {
        Lesson lesson = lessonService.findById(id);
        if (lesson != null) {
            Long courseId = lesson.getCourse().getId();
            lessonService.deleteById(id);
            return "redirect:/admin/course/" + courseId;
        }
        return "redirect:/admin/course/course-list";
    }
    @GetMapping("/course/{courseId}/select-lessons")
    public String selectLessonsForSection(@PathVariable("courseId") Long courseId,
                                          @RequestParam("sectionId") Long sectionId,
                                          Model model) {
        Course course = courseService.findByIdWithLessons(courseId);
        if (course == null) {
            return "redirect:/admin/course/course-list";
        }
        CourseSection section = courseSectionService.findById(sectionId).orElse(null);
        if (section == null || !section.getCourse().getId().equals(courseId)) {
            return "redirect:/admin/course/" + courseId;
        }

        // Get all lessons of the course
        List<Lesson> allLessons = course.getLessons();

        // Get lessons already in this section
        List<Long> assignedLessonIds = section.getLessons().stream()
                .map(Lesson::getId)
                .collect(Collectors.toList());

        model.addAttribute("course", course);
        model.addAttribute("section", section);
        model.addAttribute("allLessons", allLessons);
        model.addAttribute("assignedLessonIds", assignedLessonIds);
        model.addAttribute("activeMenu", "courses");
        return "admin/course/select-lessons";
    }
    @PostMapping("/course/{courseId}/assign-lessons")
    public String assignLessonsToSection(@PathVariable("courseId") Long courseId,
                                         @RequestParam("sectionId") Long sectionId,
                                         @RequestParam(value = "lessonIds", required = false) List<Long> lessonIds,
                                         RedirectAttributes redirectAttributes) {
        Course course = courseService.findByIdWithLessons(courseId);
        if (course == null) {
            return "redirect:/admin/course/course-list";
        }
        CourseSection section = courseSectionService.findById(sectionId).orElse(null);
        if (section == null || !section.getCourse().getId().equals(courseId)) {
            return "redirect:/admin/course/" + courseId;
        }

        // Get all lessons of the course
        List<Lesson> allLessons = course.getLessons();

        // First, remove all lessons from this section
        for (Lesson lesson : allLessons) {
            if (lesson.getSection() != null && lesson.getSection().getId().equals(sectionId)) {
                lesson.setSection(null);
                lessonService.save(lesson);
            }
        }

        // Then, assign selected lessons to this section
        if (lessonIds != null && !lessonIds.isEmpty()) {
            for (Long lessonId : lessonIds) {
                Lesson lesson = lessonService.findById(lessonId);
                if (lesson != null && lesson.getCourse().getId().equals(courseId)) {
                    lesson.setSection(section);
                    lessonService.save(lesson);
                }
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật bài học cho chương thành công.");
        return "redirect:/admin/course/" + courseId;
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewCourse(@PathVariable("id") Long id, Model model) {
        Course course = courseService.findByIdWithLessons(id);
        model.addAttribute("course", course);
        model.addAttribute("activeMenu", "courses");
        return "admin/course/admin-detail";
    }

    // --- COURSE RESOURCES MANAGEMENT ---

    @PostMapping("/course/{courseId}/add-resource")
    public String addResource(@PathVariable Long courseId,
                              @RequestParam("title") String title,
                              @RequestParam("fileType") String fileType,
                              @RequestParam(value = "externalUrl", required = false) String externalUrl,
                              @RequestParam(value = "resourceFile", required = false) org.springframework.web.multipart.MultipartFile resourceFile,
                              @RequestParam("isExternal") boolean isExternal,
                              org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            courseResourceService.addResource(courseId, title, fileType, externalUrl, resourceFile, isExternal);
            redirectAttributes.addFlashAttribute("success", "Đã thêm tài liệu thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm tài liệu: " + e.getMessage());
        }
        return "redirect:/admin/course/" + courseId;
    }

    @PostMapping("/resources/delete/{id}")
    public String deleteResource(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            if (courseResourceService.deleteResource(id)) {
                redirectAttributes.addFlashAttribute("success", "Đã xóa tài liệu!");
                // Note: courseId is not returned, but since it's in the resource, we can assume success
                return "redirect:/admin/course/course-list"; // Or find a way to get courseId, but for simplicity
            } else {
                redirectAttributes.addFlashAttribute("error", "Tài liệu không tồn tại");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa tài liệu: " + e.getMessage());
        }
        return "redirect:/admin/course/course-list";
    }
}
