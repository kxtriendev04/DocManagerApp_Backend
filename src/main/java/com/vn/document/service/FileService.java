package com.vn.document.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileService {
    @Value("${upload-file.base-uri}")
    private String baseUri;

    public void createDirectory(String folder) {
        // Không dùng URI, chỉ nối chuỗi path
        Path path = Paths.get(baseUri, folder);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println(">>> CREATED DIRECTORY: " + path.toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(">>> DIRECTORY ALREADY EXISTS");
        }
    }

    public String handleStoreFile(MultipartFile file, String folder) throws IOException {
        String finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
        Path path = Paths.get(baseUri, folder, finalName);

        Files.createDirectories(path.getParent());

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }
        return finalName;
    }

    public Resource loadFile(String folder, String filename) throws IOException {
        Path filePath = Paths.get(baseUri, folder).resolve(filename).normalize();

        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }

        try {
            return new UrlResource(filePath.toUri());
        } catch (Exception e) {
            throw new IOException("Could not read the file: " + filename, e);
        }
    }
}

