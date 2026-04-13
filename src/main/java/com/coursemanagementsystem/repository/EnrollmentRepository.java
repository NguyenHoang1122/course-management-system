package com.coursemanagementsystem.repository;


import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Enrollment findByUserIdAndCourseId(Long userId, Long CourseId);

    List<Enrollment> findByUserId(Long userId);

    @Query("select e.course from Enrollment e where e.user.id = :userId")
    List<Course> findCoursesByUserId(@Param("userId") Long userId);


    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
