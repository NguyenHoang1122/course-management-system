package com.coursemanagementsystem.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserProfileDTO {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be less than 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^(\\+?[0-9]{10,15})?$", message = "Invalid phone number")
    private String phone;

    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;

    private String avatar; // set by controller from uploaded file
}

