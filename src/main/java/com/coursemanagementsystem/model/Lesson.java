package com.coursemanagementsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "lessons")
@Data
public class Lesson {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private String videoUrl;


    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}