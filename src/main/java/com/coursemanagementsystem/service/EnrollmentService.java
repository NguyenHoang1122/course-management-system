package com.coursemanagementsystem.service;

import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.Enrollment;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public void enroll(User user, Course course) {
        Enrollment exist = enrollmentRepository.findByUserIdAndCourseId(user.getId(),
                course.getId()
        );

        if (exist != null) {
            return; // đã đăng ký rồi
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus("ENROLLED");

        enrollmentRepository.save(enrollment);

    }

    public boolean isEnrolled(Long id, Long courseId) {
        Enrollment e = enrollmentRepository.findByUserIdAndCourseId(id, courseId);
        return e != null;
    }

    public List<Enrollment> findByUserId(Long userId) {
        return enrollmentRepository.findByUserId(userId);
    }

    public List<Course> getMyCourses(String username) {
        return null;
    }

    public boolean isEnrolled(String username, Long courseId) {
        return false;
    }
}