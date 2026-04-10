package com.coursemanagementsystem.controller;

import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.service.CourseService;
import com.coursemanagementsystem.service.EnrollmentService;
import com.coursemanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private UserService userService;

    @GetMapping("")
    public String findAllCourse(Model model) {
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("pageTitle", "Course List");
        return "course/list";
    }

    @GetMapping("/{id}")
    public String viewCourse(@PathVariable Long id, Model model) {
        Course course = courseService.findByIdWithLessons(id);
        model.addAttribute("course", course);
        return "course/detail";
    }

    @GetMapping("/my-courses")
    public String myCourses(Model model, Principal principal) {
        String username = principal.getName();

        User user = userService.findByUsername(username);

        List<Course> courses = enrollmentService.getCoursesByUserId(user.getId());

        model.addAttribute("courses", courses);

        return "course/my-courses";
    }

}
