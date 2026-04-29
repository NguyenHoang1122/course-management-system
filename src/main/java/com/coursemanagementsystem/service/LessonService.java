package com.coursemanagementsystem.service;

import com.coursemanagementsystem.dto.LessonDTO;
import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.Lesson;
import com.coursemanagementsystem.repository.course.CourseRepository;
import com.coursemanagementsystem.repository.course.CourseSectionRepository;
import com.coursemanagementsystem.repository.lesson.LessonProgressRepository;
import com.coursemanagementsystem.repository.lesson.LessonRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LessonService {
    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseSectionRepository courseSectionRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private LessonProgressService lessonProgressService;
    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    public void saveFromDTO(LessonDTO dto) {

        Lesson lesson;

        if (dto.getId() != null) {
            lesson = lessonRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Lesson not found"));
            modelMapper.map(dto, lesson);
        } else {
            // create
            lesson = modelMapper.map(dto, Lesson.class);
        }

        Course course = courseRepository.findById(dto.getCourseId()).orElse(null);
        lesson.setCourse(course);

        if (dto.getSectionId() != null) {
            lesson.setSection(courseSectionRepository.findById(dto.getSectionId()).orElse(null));
        }

        lessonRepository.save(lesson);
    }

    public Lesson findById(Long id) {
        return lessonRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        lessonRepository.deleteById(id);
    }

    public void save(Lesson lesson) {
        lessonRepository.save(lesson);
    }


    public List<Lesson> findAll() {
        return lessonRepository.findAll();
    }

    public List<Lesson> searchLessons(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return lessonRepository.findAll();
        }
        return lessonRepository.searchLessons(keyword.trim());
    }

    public Lesson getLessonForUser(Long lessonId, Long userId) {

        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);

        if (lesson == null) {
            return null;
        }
        if (lesson.getCourse() == null) {
            return null;
        }

        boolean enrolled = enrollmentService.isEnrolled(userId, lesson.getCourse().getId());

        if (!enrolled) {
            return null;
        }

        return lesson;
    }
}