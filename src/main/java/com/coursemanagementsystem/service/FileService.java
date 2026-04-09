package com.coursemanagementsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadFile(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new RuntimeException("File rỗng");
        }

        // chỉ cho phép ảnh
        if (!file.getContentType().startsWith("image/")) {
            throw new RuntimeException("Chỉ cho phép upload ảnh");
        }

        // tạo folder nếu chưa có
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // rename tránh trùng
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        File saveFile = new File(uploadDir + "/" + fileName);
        file.transferTo(saveFile);

        // trả về path để lưu DB
        return "/uploads/" + fileName;
    }
}
