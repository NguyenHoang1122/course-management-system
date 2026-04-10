package com.coursemanagementsystem.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO {
    private Long id;
    private String userName;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private String avatar;
    private LocalDate dateOfBirth;
    private String address;

    private String roleName;
}
