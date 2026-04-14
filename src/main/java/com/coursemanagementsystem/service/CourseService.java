package com.coursemanagementsystem.service;

import com.coursemanagementsystem.dto.CourseDTO;
import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.CourseRepository;
import com.coursemanagementsystem.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    public void saveFromDTO(CourseDTO dto) {

        Course course;

        if (dto.getId() != null) {
            // Lấy entity từ DB
            course = courseRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            //  Map vào object cũ
            modelMapper.map(dto, course);

        } else {
            // CREATE
            course = modelMapper.map(dto, Course.class);
            course.setCreatedAt(LocalDate.now());
        }

        // 👉 xử lý relation riêng
        if (dto.getInstructorId() != null) {
            User instructor = userRepository.findByIdAndDeletedFalse(dto.getInstructorId()).orElse(null);
            course.setInstructor(instructor);
        }

        courseRepository.save(course);
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Page<Course> findCoursesPaged(String keyword, int page, int size) {
        int normalizedPage = Math.max(page - 1, 0);
        int normalizedSize = normalizePageSize(size);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);

        if (keyword == null || keyword.trim().isEmpty()) {
            return courseRepository.findAll(pageable);
        }

        return courseRepository.searchCourses(keyword.trim(), pageable);
    }

    private int normalizePageSize(int size) {
        if (size == 20 || size == 50) {
            return size;
        }
        return 10;
    }

    public Course findById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    public CourseDTO findDTOById(Long id) {
        Course course = courseRepository.findById(id).orElse(null);

        if (course == null) return null;

        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setPrice(course.getPrice());

        if (course.getInstructor() != null) {
            dto.setInstructorId(course.getInstructor().getId());
        }

        // Set lessons để hiển thị số bài học trong preview
        dto.setLessons(course.getLessons());

        return dto;
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }

    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }

    public Course findByIdWithLessons(Long id) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course != null) {
            course.getLessons().size(); // trigger loading
        }
        return course;
    }
}