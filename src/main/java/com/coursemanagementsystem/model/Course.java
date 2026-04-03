package com.coursemanagementsystem.model;

import jakarta.persistence.*;
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

    private String title; //ten khoa hoc
    private String description; //mieu ta khoa hoc
    private Double price; //gia

    private LocalDate createdAt; //Ngay tao khoa hoc

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @OneToMany(mappedBy = "course")
    private List<Lesson> lessons;
}