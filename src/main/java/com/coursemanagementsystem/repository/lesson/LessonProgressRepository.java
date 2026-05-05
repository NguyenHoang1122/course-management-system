package com.coursemanagementsystem.repository.lesson;

import com.coursemanagementsystem.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    @Query("select lp.lesson.id from LessonProgress lp where lp.user.id = :userId and lp.lesson.course.id = :courseId")
    List<Long> findCompletedLessonIdsByUserAndCourse(@Param("userId") Long userId,
                                                      @Param("courseId") Long courseId);

    @Modifying
    @Query("delete from LessonProgress lp where lp.user.id in :userIds")
    int deleteByUserIds(@Param("userIds") List<Long> userIds);

    long countByUserIdAndLessonCourseId(Long userId, Long courseId);
}

