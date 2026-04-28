package com.coursemanagementsystem.service;

import com.coursemanagementsystem.model.Course;
import com.coursemanagementsystem.model.CourseResource;
import com.coursemanagementsystem.repository.course.CourseResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class CourseResourceService {

    @Autowired
    private CourseResourceRepository courseResourceRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private FileService fileService;

    public CourseResource addResource(Long courseId, String title, String fileType, String externalUrl, MultipartFile resourceFile, boolean isExternal) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        CourseResource resource = new CourseResource();
        resource.setTitle(title);
        resource.setFileType(fileType);
        resource.setCourse(course);
        resource.setExternal(isExternal);

        if (isExternal) {
            if (externalUrl == null || externalUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("External URL cannot be empty");
            }
            resource.setUrl(externalUrl.trim());
        } else {
            if (resourceFile == null || resourceFile.isEmpty()) {
                throw new IllegalArgumentException("Resource file cannot be empty");
            }
            try {
                String fileUrl = fileService.uploadResource(resourceFile);
                resource.setUrl(fileUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload resource file", e);
            }
        }

        return courseResourceRepository.save(resource);
    }

    public boolean deleteResource(Long id) {
        Optional<CourseResource> resourceOpt = courseResourceRepository.findById(id);
        if (resourceOpt.isPresent()) {
            CourseResource resource = resourceOpt.get();
            // If local file, delete it from disk
            if (!resource.isExternal()) {
                fileService.deleteFile(resource.getUrl());
            }
            courseResourceRepository.delete(resource);
            return true;
        }
        return false;
    }

    public Optional<CourseResource> findById(Long id) {
        return courseResourceRepository.findById(id);
    }

    public void delete(CourseResource resource) {
        courseResourceRepository.delete(resource);
    }
}
