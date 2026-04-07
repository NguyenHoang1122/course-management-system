package com.coursemanagementsystem.dto;

import lombok.Data;

@Data
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private Double price;

    private Long instructorId;
}
