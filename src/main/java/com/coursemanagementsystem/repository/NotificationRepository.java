package com.coursemanagementsystem.repository;

import com.coursemanagementsystem.model.Notification;
import com.coursemanagementsystem.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    //   danh sách thông báo theo time
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // tat ca thong báo chưa đọc
    List<Notification> findAllByUserIdAndIsReadFalse(Long userId);

    //    Đếm số lượng thông báo chưa đọc
    long countByUserIdAndIsReadFalse(Long userId);
}
