package com.coursemanagementsystem.repository;

import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.Enrollment;
import com.coursemanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByUserAndCourse(User user, Course course);
    List<Enrollment> findByUser(User user);

}
