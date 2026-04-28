package com.coursemanagementsystem.service;

import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.CourseSection;
import com.coursemanagementsystem.repository.course.CourseSectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseSectionService {

    @Autowired
    private CourseSectionRepository courseSectionRepository;

    @Autowired
    private CourseService courseService; // Inject để lấy course trong addSection

    public Optional<CourseSection> findById(Long id) {
        return courseSectionRepository.findById(id);
    }

    public List<CourseSection> findByCourseIdOrderByDisplayOrderAsc(Long courseId) {
        return courseSectionRepository.findByCourseIdOrderByDisplayOrderAsc(courseId);
    }

    public CourseSection save(CourseSection section) {
        return courseSectionRepository.save(section);
    }

    public void deleteById(Long id) {
        courseSectionRepository.deleteById(id);
    }

    public void delete(CourseSection section) {
        courseSectionRepository.delete(section);
    }

    public CourseSection addSection(Long courseId, String title) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }
        CourseSection section = new CourseSection();
        section.setTitle(title);
        section.setCourse(course);
        // Get current max order
        int maxOrder = course.getSections().size();
        section.setDisplayOrder(maxOrder + 1);

        return save(section);
    }

    public CourseSection updateSection(Long id, String title) {
        CourseSection section = findById(id).orElse(null);
        if (section != null) {
            section.setTitle(title);
            return save(section);
        }
        return null;
    }

    public boolean deleteSection(Long id) {
        CourseSection section = findById(id).orElse(null);
        if (section != null) {
            deleteById(id);
            return true;
        }
        return false;
    }
}
