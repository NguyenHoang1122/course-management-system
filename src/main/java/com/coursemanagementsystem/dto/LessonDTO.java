package com.coursemanagementsystem.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LessonDTO {
    private Long id;

    @NotBlank(message = "Tên bài học không được để trống")
    @Size(min = 3, max = 200, message = "Tên bài học phải từ 3 đến 200 ký tự")
    private String title;

    // videoUrl là tùy chọn - giảng viên có thể thêm sau
    @Size(max = 500, message = "URL video không được vượt quá 500 ký tự")
    @Pattern(
        regexp = "^$|^(https?://).+",
        message = "URL video phải bắt đầu bằng http:// hoặc https://"
    )
    private String videoUrl;

    @NotNull(message = "Khóa học không được để trống")
    private Long courseId;

    private Long sectionId;
}