package com.coursemanagementsystem.service;

import com.coursemanagementsystem.dto.CourseDTO;
import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.UserRepository;
import com.coursemanagementsystem.repository.course.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
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
            course.setPreviewVideoUrl(dto.getPreviewVideoUrl());
            course.setCategory(dto.getCategory());
            course.setLevel(dto.getLevel());
            course.setDuration(dto.getDuration());
            course.setLearningPoints(dto.getLearningPoints());
            course.setRequirements(dto.getRequirements());
            course.setTargetAudience(dto.getTargetAudience());

        } else {
            // CREATE
            course = new Course();
            course.setTitle(dto.getTitle());
            course.setDescription(dto.getDescription());
            course.setPrice(dto.getPrice());
            course.setImageUrl(dto.getImageUrl());
            course.setPreviewVideoUrl(dto.getPreviewVideoUrl());
            course.setCategory(dto.getCategory());
            course.setLevel(dto.getLevel());
            course.setDuration(dto.getDuration());
            course.setLearningPoints(dto.getLearningPoints());
            course.setRequirements(dto.getRequirements());
            course.setTargetAudience(dto.getTargetAudience());
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

    public List<Course> searchCoursesByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return courseRepository.findAll();
        }
        return courseRepository.searchCourses(keyword.trim(), Pageable.unpaged()).getContent();
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
        int normalizedSize = size; // Honor the size passed from controller
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);

        List<Course> courses;

        // Get base courses - apply keyword filter first if provided
        if (keyword != null && !keyword.trim().isEmpty()) {
            // searchCourses already returns results ordered by relevance
            courses = courseRepository.searchCourses(keyword.trim(), Pageable.unpaged()).getContent();
        } else {
            courses = courseRepository.findAll();
        }

        // Apply sort (only if it's not the default relevance-based sort from search)
        if (sortBy != null && !sortBy.isEmpty() && !(keyword != null && !keyword.trim().isEmpty())) {
            courses = sortCourses(courses, sortBy);
        } else if (sortBy != null && !sortBy.isEmpty() && !"newest".equals(sortBy)) {
            // If keyword exists and user selected a sort, apply that sort
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
        if (size == 6 || size == 9 || size == 10 || size == 20 || size == 50) {
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
        dto.setPreviewVideoUrl(course.getPreviewVideoUrl());
        dto.setCategory(course.getCategory());
        dto.setLevel(course.getLevel());
        dto.setDuration(course.getDuration());
        dto.setLearningPoints(course.getLearningPoints());
        dto.setRequirements(course.getRequirements());
        dto.setTargetAudience(course.getTargetAudience());

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
            // Khởi tạo các chương và bài học bên trong
            if (course.getSections() != null) {
                course.getSections().forEach(section -> {
                    if (section.getLessons() != null) {
                        section.getLessons().size();
                    }
                });
            }
            // Giữ lại lessons list cho tương thích ngược (legacy)
            if (course.getLessons() != null) {
                course.getLessons().size();
            }
        }
        return course;
    }

    public long countTotalCourses() {
        return courseRepository.countTotalCourses();
    }

    public long countFreeCourses() {
        return courseRepository.countFreeCourses();
    }

    // --- AUTOMATIC DURATION CALCULATION ---

    public String getAutoDuration(Course course) {
        if (course == null) return "0m";

        long totalSeconds = 0;
        List<com.coursemanagementsystem.model.Lesson> lessons = course.getLessons();
        
        // Fallback to sections if lessons list is empty
        if ((lessons == null || lessons.isEmpty()) && course.getSections() != null) {
            for (com.coursemanagementsystem.model.CourseSection section : course.getSections()) {
                if (section.getLessons() != null) {
                    for (com.coursemanagementsystem.model.Lesson lesson : section.getLessons()) {
                        totalSeconds += parseDurationToSeconds(lesson.getDuration());
                    }
                }
            }
        } else if (lessons != null) {
            for (com.coursemanagementsystem.model.Lesson lesson : lessons) {
                totalSeconds += parseDurationToSeconds(lesson.getDuration());
            }
        }

        if (totalSeconds == 0) return "0m";
        return formatSecondsToHumanReadable(totalSeconds);
    }

    private long parseDurationToSeconds(String duration) {
        if (duration == null || duration.trim().isEmpty()) return 0;
        try {
            String[] parts = duration.split(":");
            if (parts.length == 2) {
                // mm:ss
                return Long.parseLong(parts[0]) * 60 + Long.parseLong(parts[1]);
            } else if (parts.length == 3) {
                // hh:mm:ss
                return Long.parseLong(parts[0]) * 3600 + Long.parseLong(parts[1]) * 60 + Long.parseLong(parts[2]);
            }
        } catch (NumberFormatException e) {
            // Log error or ignore if format is invalid
        }
        return 0;
    }

    private String formatSecondsToHumanReadable(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(" giờ ");
        }
        if (minutes > 0 || (hours == 0 && seconds > 0)) {
            sb.append(minutes).append(" phút");
        }
        
        String result = sb.toString().trim();
        return result.isEmpty() ? "0m" : result;
    }
}

