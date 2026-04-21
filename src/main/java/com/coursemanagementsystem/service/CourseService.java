package com.coursemanagementsystem.service;

import com.coursemanagementsystem.dto.CourseDTO;
import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.CourseRepository;
import com.coursemanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    public void saveFromDTO(CourseDTO dto) {

        Course course;

        if (dto.getId() != null) {
            // UPDATE
            course = courseRepository.findById(dto.getId()).orElse(null);

            if (course == null) {
                throw new RuntimeException("Course not found");
            }

            // map thủ công (AN TOÀN - không đụng tới relation)
            course.setTitle(dto.getTitle());
            course.setDescription(dto.getDescription());
            course.setPrice(dto.getPrice());
            course.setImageUrl(dto.getImageUrl());
            course.setCategory(dto.getCategory());

        } else {
            // CREATE
            course = new Course();
            course.setTitle(dto.getTitle());
            course.setDescription(dto.getDescription());
            course.setPrice(dto.getPrice());
            course.setImageUrl(dto.getImageUrl());
            course.setCategory(dto.getCategory());
            course.setCreatedAt(LocalDate.now());
        }

        // xử lý instructor (QUAN TRỌNG)
        if (dto.getInstructorId() != null) {
            User instructor = userRepository.findByIdAndDeletedFalse(dto.getInstructorId()).orElse(null);

            if (instructor == null) {
                throw new RuntimeException("Instructor không tồn tại");
            }

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

    public Page<Course> findCoursesFiltered(String keyword, Double minPrice, Double maxPrice, String sortBy, int page, int size) {
        int normalizedPage = Math.max(page - 1, 0);
        int normalizedSize = normalizePageSize(size);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);

        List<Course> courses;

        // Get base courses
        courses = courseRepository.findAll();


        // Apply sort
        if (sortBy != null && !sortBy.isEmpty()) {
            courses = sortCourses(courses, sortBy);
        }

        // Apply pagination manually
        int total = courses.size();
        int start = normalizedPage * normalizedSize;
        int end = Math.min(start + normalizedSize, total);

        if (start >= total) {
            courses = List.of();
        } else {
            courses = courses.subList(start, end);
        }

        return new PageImpl<>(courses, pageable, total);
    }

    private List<Course> sortCourses(List<Course> courses, String sortBy) {
        return switch (sortBy) {
            case "price_low_high" -> courses.stream()
                    .sorted((a, b) -> {
                        Double priceA = a.getPrice() != null ? a.getPrice().doubleValue() : 0.0;
                        Double priceB = b.getPrice() != null ? b.getPrice().doubleValue() : 0.0;
                        return priceA.compareTo(priceB);
                    })
                    .toList();
            case "price_high_low" -> courses.stream()
                    .sorted((a, b) -> {
                        Double priceA = a.getPrice() != null ? a.getPrice().doubleValue() : 0.0;
                        Double priceB = b.getPrice() != null ? b.getPrice().doubleValue() : 0.0;
                        return priceB.compareTo(priceA);
                    })
                    .toList();
            case "newest" -> courses.stream()
                    .sorted((a, b) -> {
                        LocalDate dateA = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDate.MIN;
                        LocalDate dateB = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDate.MIN;
                        return dateB.compareTo(dateA);
                    })
                    .toList();
            case "oldest" -> courses.stream()
                    .sorted((a, b) -> {
                        LocalDate dateA = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDate.MIN;
                        LocalDate dateB = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDate.MIN;
                        return dateA.compareTo(dateB);
                    })
                    .toList();
            default -> courses;
        };
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
        dto.setImageUrl(course.getImageUrl());
        dto.setCategory(course.getCategory());

        if (course.getInstructor() != null) {
            dto.setInstructorId(course.getInstructor().getId());
        }

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
            course.getLessons().size();
        }
        return course;
    }
}