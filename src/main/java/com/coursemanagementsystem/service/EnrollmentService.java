package com.coursemanagementsystem.service;

import com.coursemanagementsystem.model.Course;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {

    public void enroll(String username, Long courseId) {
    }

    public List<Course> getMyCourses(String username) {
        return null;
    }

    public boolean isEnrolled(String username, Long courseId) {
        return false;
    }
}