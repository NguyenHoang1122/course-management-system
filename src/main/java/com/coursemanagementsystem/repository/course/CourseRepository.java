package com.coursemanagementsystem.repository.course;

import com.coursemanagementsystem.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTitleContaining(String keyword);

    @Query("""
            select c from Course c
            left join c.instructor i
            where lower(c.title) like lower(concat('%', :keyword, '%'))
               or lower(c.description) like lower(concat('%', :keyword, '%'))
               or lower(i.fullName) like lower(concat('%', :keyword, '%'))
            order by 
                case when lower(c.title) = lower(:keyword) then 0
                     when lower(c.title) like lower(concat(:keyword, '%')) then 1
                     when lower(c.title) like lower(concat('%', :keyword, '%')) then 2
                     else 3
                end,
                c.title asc
            """)
    Page<Course> searchCourses(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query("update Course c set c.instructor = null where c.instructor.id in :userIds")
    int clearInstructorForUserIds(@Param("userIds") List<Long> userIds);

    @Query("select count(c) from Course c")
    long countTotalCourses();

    @Query("select count(c) from Course c where c.price = 0 or c.price is null")
    long countFreeCourses();
}
