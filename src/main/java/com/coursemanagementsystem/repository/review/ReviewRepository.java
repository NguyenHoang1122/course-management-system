package com.coursemanagementsystem.repository.review;

import com.coursemanagementsystem.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByCourseId(Long courseId);

    Optional<Review> findByUserIdAndCourseId(Long userId, Long courseId);

    @Modifying
    @Query("delete from Review r where r.user.id in :userIds")
    int deleteByUserIds(@Param("userIds") List<Long> userIds);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.course.id = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);

    List<Review> findTop3ByOrderByCreatedAtDesc();

}
