package com.coursemanagementsystem.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private Long id;
    private String userName;
    private String password;
    private String fullName;
    private String email;
}
