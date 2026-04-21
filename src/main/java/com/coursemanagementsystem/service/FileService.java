package com.coursemanagementsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_RESOURCE_EXTENSIONS = Set.of("pdf", "zip", "rar", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "mp3", "mp4");

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadFile(MultipartFile file) throws IOException {
        return uploadInternal(file, ALLOWED_IMAGE_EXTENSIONS, "image/", "Only image files are allowed");
    }

    public String uploadResource(MultipartFile file) throws IOException {
        return uploadInternal(file, ALLOWED_RESOURCE_EXTENSIONS, null, "Unsupported file format for resources");
    }

    private String uploadInternal(MultipartFile file, Set<String> allowedExtensions, String contentTypePrefix, String errorMessage) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (contentTypePrefix != null) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith(contentTypePrefix)) {
                throw new IllegalArgumentException(errorMessage);
            }
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalName);

        if (extension == null || !allowedExtensions.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(errorMessage);
        }

        Path uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String fileName = UUID.randomUUID() + "." + extension.toLowerCase();
        Path savePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + fileName;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return;
        }

        try {
            String fileName = fileUrl.substring("/uploads/".length());
            Path filePath = Path.of(uploadDir).resolve(fileName).toAbsolutePath().normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + fileUrl + ". Error: " + e.getMessage());
        }
    }
}
