package com.coursemanagementsystem.repository;


import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Enrollment findByUserIdAndCourseId(Long userId, Long CourseId);

    List<Enrollment> findByUserId(Long userId);

    @Query("select e.course from Enrollment e where e.user.id = :userId")
    List<Course> findCoursesByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from Enrollment e where e.user.id in :userIds")
    int deleteByUserIds(@Param("userIds") List<Long> userIds);


    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    long countByCourseId(Long courseId);

    @Query("select count(distinct e.user.id) from Enrollment e")
    long countDistinctStudents();

    @Query("select count(distinct e.user.id) from Enrollment e where e.status = 'COMPLETED'")
    long countGraduates();
}
