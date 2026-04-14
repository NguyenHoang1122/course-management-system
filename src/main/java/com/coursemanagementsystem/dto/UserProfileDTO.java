package com.coursemanagementsystem.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserProfileDTO {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    @Pattern(
        regexp = "^[\\p{L} ]+$",
        message = "Họ tên không được chứa số hoặc ký tự đặc biệt"
    )
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng (ví dụ: name@gmail.com)")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Pattern(
        regexp = "^(\\+?[0-9]{10,15})?$",
        message = "Số điện thoại không hợp lệ (10-15 chữ số, có thể bắt đầu bằng +)"
    )
    private String phone;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    private String avatar; // set by controller from uploaded file
}
