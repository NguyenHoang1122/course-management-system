package com.coursemanagementsystem.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CourseDTO {
    private Long id;

    @NotBlank(message = "Tên khóa học không được để trống")
    @Size(min = 3, max = 200, message = "Tên khóa học phải từ 3 đến 200 ký tự")
    private String title;

    @NotBlank(message = "Mô tả khóa học không được để trống")
    @Size(min = 10, max = 2000, message = "Mô tả phải từ 10 đến 2000 ký tự")
    private String description;

    @NotNull(message = "Giá khóa học không được để trống")
    @PositiveOrZero(message = "Giá khóa học phải là số không âm")
    @DecimalMax(value = "999999999", message = "Giá khóa học không được vượt quá 999,999,999 VND")
    private Double price;

    @NotNull(message = "Vui lòng chọn giảng viên")
    private Long instructorId;

    private String imageUrl;
    private String previewVideoUrl;
    private String category;

    // --- NEW PREMIUM FIELDS ---
    private String level;
    private String duration;
    private String learningPoints;
    private String requirements;
    private String targetAudience;

    private List<?> lessons;
}
