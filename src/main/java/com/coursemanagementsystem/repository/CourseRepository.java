package com.coursemanagementsystem.repository;

import com.coursemanagementsystem.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTitleContaining(String keyword);
}
