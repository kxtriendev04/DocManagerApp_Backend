package com.vn.document.service;

import com.vn.document.domain.Document;
import com.vn.document.repository.DocumentRepository;
import com.vn.document.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {
    @Value("${upload-file.base-uri}")
    private String baseUri;

    @Autowired
    private DocumentRepository documentRepository;

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

    public String handleStoreFile(MultipartFile file, String folder, String password) throws IOException {
        String finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
        Path path = Paths.get(baseUri, folder, finalName);

        Files.createDirectories(path.getParent());

        try {
            byte[] originalBytes = file.getBytes();
            byte[] encryptedBytes = AESUtil.encrypt(originalBytes, password);
            Files.write(path, encryptedBytes);
            return "/storage/" + folder + "/" + finalName;
        } catch (Exception e) {
            throw new IOException("Encryption failed", e);
        }
    }

    public Resource loadFile(String folder, String filename, String password) throws IOException {
        Path filePath = Paths.get(baseUri, folder).resolve(filename).normalize();

        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }

        String fileUrl = "/storage/" + folder + "/" + filename;
        Document document = documentRepository.findByFileUrl(fileUrl);
        if (document == null) {
            throw new IOException("Document metadata not found for file: " + fileUrl);
        }

        if (!BCrypt.checkpw(password, document.getPassword())) {
            throw new IOException("Invalid password");
        }

        try {
            byte[] encryptedBytes = Files.readAllBytes(filePath);
            byte[] decryptedBytes = AESUtil.decrypt(encryptedBytes, password);
            return new ByteArrayResource(decryptedBytes);
        } catch (Exception e) {
            throw new IOException("Could not decrypt the file: " + filename, e);
        }
    }

    public void deleteFile(String fileUrl) throws IOException {
        // Chuyển fileUrl thành đường dẫn vật lý
        if (fileUrl == null || !fileUrl.startsWith("/storage/")) {
            throw new IOException("Invalid file URL: " + fileUrl);
        }

        // Loại bỏ phần "/storage/" để lấy folder và filename
        String relativePath = fileUrl.substring("/storage/".length());
        Path filePath = Paths.get(baseUri, relativePath).normalize();

        if (Files.exists(filePath)) {
            try {
                Files.delete(filePath);
                System.out.println(">>> DELETED FILE: " + filePath.toAbsolutePath());
            } catch (IOException e) {
                throw new IOException("Could not delete file: " + filePath, e);
            }
        } else {
            System.out.println(">>> FILE NOT FOUND: " + filePath.toAbsolutePath());
        }
    }
}

