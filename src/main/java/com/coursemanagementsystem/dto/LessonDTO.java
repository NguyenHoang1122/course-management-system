package com.coursemanagementsystem.dto;

import lombok.Data;

@Data
public class LessonDTO {
    private Long id;
    private String title;
    private String videoUrl;
    private Long courseId;
}