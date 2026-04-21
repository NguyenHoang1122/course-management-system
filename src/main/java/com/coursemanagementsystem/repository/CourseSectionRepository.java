package com.coursemanagementsystem.repository;

import com.coursemanagementsystem.model.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {
    List<CourseSection> findByCourseIdOrderByDisplayOrderAsc(Long courseId);
}
