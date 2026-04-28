package com.coursemanagementsystem.repository.review;

import com.coursemanagementsystem.model.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
}
