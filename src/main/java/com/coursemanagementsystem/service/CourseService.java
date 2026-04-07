package com.coursemanagementsystem.service;

import com.coursemanagementsystem.dto.CourseDTO;
import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.CourseRepository;
import com.coursemanagementsystem.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void saveFromDTO(CourseDTO courseDTO) {
        Course course = modelMapper.map(courseDTO, Course.class);

        if (courseDTO.getInstructorId() != null) {
            User instructor = userRepository.findById(courseDTO.getInstructorId()).orElse(null);
            course.setInstructor(instructor);
        }
        course.setCreatedAt(LocalDate.now());
        courseRepository.save(course);
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
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

        return dto;
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }

   public void deleteById(Long id) {
        courseRepository.deleteById(id);
   }
}