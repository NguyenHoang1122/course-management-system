package com.coursemanagementsystem.service;

import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.Enrollment;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.EnrollmentRepository;
import com.coursemanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

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
        enrollment.setEnrolledAt(LocalDateTime.now());

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

  /*  public boolean isEnrolled(String username, Long courseId) {
        return false;
    }*/

    public List<Course> getCoursesByUserId(Long userId) {
        return enrollmentRepository.findCoursesByUserId(userId);
    }


    /*public boolean isEnrolled(String username, Long courseId) {

        User user = userRepository.findByUserName(username);

        if (user == null) {
            return false;
        }

        Enrollment e = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId);

        if (e == null) {
            return false;
        }

        return true;
    }*/

    public boolean isEnrolled(String username, Long courseId) {
        Optional<User> optionalUser = userRepository.findByUserName(username);
        if (optionalUser.isEmpty()) {
            return false;
        }

        return isEnrolled(optionalUser.get().getId(), courseId);
    }

    public boolean isUserEnrolled(Long userId, Long courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId((userId), courseId);
    }

    public long countEnrollmentsByCourseId(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }
}