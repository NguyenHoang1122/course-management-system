package com.coursemanagementsystem.dto;

import com.coursemanagementsystem.model.Enrollment;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnrollmentCourseProgressDTO {
    private Enrollment enrollment;
    private long completedLessons;
    private long totalLessons;
    private int progressPercent;
    private String learningStatus;
}

