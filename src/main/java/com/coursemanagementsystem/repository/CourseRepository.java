package com.coursemanagementsystem.repository;

import com.coursemanagementsystem.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
