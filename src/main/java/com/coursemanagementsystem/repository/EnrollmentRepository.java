package com.coursemanagementsystem.repository;


import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Enrollment findByUserIdAndCourseId(Long userId, Long CourseId);

    List<Enrollment> findByUserId(Long userId);

    List<Course> findCoursesByUserId(Long userId);


    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
