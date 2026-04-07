package com.coursemanagementsystem.service;

import com.coursemanagementsystem.dto.LessonDTO;
import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.Lesson;
import com.coursemanagementsystem.repository.CourseRepository;
import com.coursemanagementsystem.repository.LessonRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LessonService {
    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModelMapper modelMapper;

    public void saveFromDTO(LessonDTO dto) {

        Lesson lesson;

        if (dto.getId() != null) {
            // update
            lesson = lessonRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Lesson not found"));
            modelMapper.map(dto, lesson);
        } else {
            // create
            lesson = modelMapper.map(dto, Lesson.class);
        }

        Course course = courseRepository.findById(dto.getCourseId()).orElse(null);
        lesson.setCourse(course);

        lessonRepository.save(lesson);
    }
}
