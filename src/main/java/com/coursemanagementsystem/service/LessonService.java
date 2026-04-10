package com.coursemanagementsystem.service;

import com.coursemanagementsystem.dto.LessonDTO;
import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.Lesson;
import com.coursemanagementsystem.repository.CourseRepository;
import com.coursemanagementsystem.repository.EnrollmentRepository;
import com.coursemanagementsystem.repository.LessonRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public LessonService(LessonRepository lessonRepository, EnrollmentRepository enrollmentRepository,
                         CourseRepository courseRepository){
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }


    public void saveFromDTO(LessonDTO dto) {

        Lesson lesson;

        // UPDATE
        if (dto.getId() != null) {
            lesson = lessonRepository.findById(dto.getId())
                    .orElse(new Lesson());
        }
        // CREATE
        else {
            lesson = new Lesson();
        }

        lesson.setTitle(dto.getTitle());
        lesson.setVideoUrl(dto.getVideoUrl());

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        lesson.setCourse(course);

        lessonRepository.save(lesson);
    }

    public Lesson findById(Long id) {
        return lessonRepository.findById(id)
                .orElse(null);
    }

    public void deleteById(Long id) {
        lessonRepository.deleteById(id);
    }

    public Lesson getLessonForUser(Long lessonId, Long userId) {

        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);

        if (lesson == null) {
            return null;
        }

        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, lesson.getCourse().getId());

        if (!isEnrolled) {
            throw new RuntimeException("Bạn chưa đăng ký khóa học này");
        }

        return lesson;
    }
}