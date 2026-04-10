package com.coursemanagementsystem.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LessonDTO {
    private Long id;

    @NotBlank(message = "Lesson title is required")
    @Size(max = 200, message = "Lesson title must be less than 200 characters")
    private String title;

    @NotBlank(message = "Video URL is required")
    private String videoUrl;

    private Long courseId;
}