package com.coursemanagementsystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private Double price;

    private Long instructorId;
    
    private List<?> lessons;
}
