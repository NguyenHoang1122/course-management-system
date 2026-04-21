package com.coursemanagementsystem.service;

import com.coursemanagementsystem.model.Lesson;
import com.coursemanagementsystem.model.LessonProgress;
import com.coursemanagementsystem.model.User;
import com.coursemanagementsystem.repository.LessonProgressRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;

    public LessonProgressService(LessonProgressRepository lessonProgressRepository) {
        this.lessonProgressRepository = lessonProgressRepository;
    }

    public boolean isCompleted(Long userId, Long lessonId) {
        return lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId).isPresent();
    }

    public void markCompleted(User user, Lesson lesson) {
        if (lessonProgressRepository.findByUserIdAndLessonId(user.getId(), lesson.getId()).isPresent()) {
            return;
        }

        LessonProgress progress = new LessonProgress();
        progress.setUser(user);
        progress.setLesson(lesson);
        progress.setCompletedAt(LocalDateTime.now());
        lessonProgressRepository.save(progress);
    }

    public Set<Long> getCompletedLessonIds(Long userId, Long courseId) {
        return new HashSet<>(lessonProgressRepository.findCompletedLessonIdsByUserAndCourse(userId, courseId));
    }

    public long countCompletedLessons(Long userId, Long courseId) {
        return lessonProgressRepository.countByUserIdAndLessonCourseId(userId, courseId);
    }

    public boolean toggleProgress(User user, Lesson lesson) {
        var existing = lessonProgressRepository.findByUserIdAndLessonId(user.getId(), lesson.getId());
        if (existing.isPresent()) {
            lessonProgressRepository.delete(existing.get());
            return false; // Result: NOT COMPLETED
        } else {
            LessonProgress progress = new LessonProgress();
            progress.setUser(user);
            progress.setLesson(lesson);
            progress.setCompletedAt(LocalDateTime.now());
            lessonProgressRepository.save(progress);
            return true; // Result: COMPLETED
        }
    }
}

