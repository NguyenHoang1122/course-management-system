package com.coursemanagementsystem.repository.lesson;

import com.coursemanagementsystem.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseId(Long courseId);

    @Query("SELECT l FROM Lesson l WHERE LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(l.course.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Lesson> searchLessons(@Param("keyword") String keyword);
}