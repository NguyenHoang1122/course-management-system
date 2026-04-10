package com.coursemanagementsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Course title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title; //ten khoa hoc

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description; //mieu ta khoa hoc

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be zero or positive")
    private Double price; //gia

    private LocalDate createdAt; //Ngay tao khoa hoc

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @OneToMany(mappedBy = "course",fetch = FetchType.LAZY)
    private List<Lesson> lessons;
}