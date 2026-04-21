package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.dto.CourseDTO;
import com.coursemanagementsystem.dto.LessonDTO;
import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.Lesson;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.CourseService;
import com.coursemanagementsystem.service.LessonService;
import com.coursemanagementsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    private LessonService lessonService;
    @Autowired
    private com.coursemanagementsystem.repository.CourseSectionRepository courseSectionRepository;

    @GetMapping("/course-list")
    public String findALlCourseList(@RequestParam(value = "page", defaultValue = "1") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
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

        model.addAttribute("courses", paginatedCourses);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalCourses);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("totalLessons", totalLessons);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "courses");
        return "admin/course-list";
    }

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
    public String lessonList(@RequestParam(value = "page", defaultValue = "1") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
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

    @GetMapping("/create-lesson")
    public String createLesson(Model model) {
        model.addAttribute("lessonDTO", new LessonDTO());
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("activeMenu", "lessons");
        return "admin/add-lesson";
    }
}
