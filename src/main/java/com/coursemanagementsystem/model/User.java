package com.coursemanagementsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private String avatar; // URL ảnh đại diện
    private LocalDate dateOfBirth;
    private String address;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private List<Enrollment> enrollments;

}
