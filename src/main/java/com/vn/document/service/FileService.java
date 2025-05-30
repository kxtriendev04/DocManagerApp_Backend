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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
public class FileService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private DocumentRepository documentRepository;

    public String handleStoreFile(MultipartFile file, String folder, String password) throws IOException {
        String finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
        String s3Key = folder + "/" + finalName; // Tạo key cho S3 (e.g., "folder/filename")

        try {
            byte[] originalBytes = file.getBytes();
            byte[] encryptedBytes = AESUtil.encrypt(originalBytes, password);

            // Lưu tệp tin mã hóa lên S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(encryptedBytes));

            return "/storage/" + folder + "/" + finalName; // Trả về fileUrl cho Document
        } catch (Exception e) {
            throw new IOException("Encryption or S3 upload failed", e);
        }
    }

    public Resource loadFile(String folder, String filename, String password) throws IOException {
        String s3Key = folder + "/" + filename;

        // Kiểm tra metadata
        String fileUrl = "/storage/" + folder + "/" + filename;
        Document document = documentRepository.findByFileUrl(fileUrl);
        if (document == null) {
            throw new IOException("Document metadata not found for file: " + fileUrl);
        }

        if (!BCrypt.checkpw(password, document.getPassword())) {
            throw new IOException("Invalid password");
        }

        try {
            // Tải tệp tin từ S3
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            byte[] encryptedBytes = s3Client.getObject(getObjectRequest).readAllBytes();

            // Giải mã
            byte[] decryptedBytes = AESUtil.decrypt(encryptedBytes, password);
            return new ByteArrayResource(decryptedBytes);
        } catch (Exception e) {
            throw new IOException("Could not decrypt or download file from S3: " + filename, e);
        }
    }

    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || !fileUrl.startsWith("/storage/")) {
            throw new IOException("Invalid file URL: " + fileUrl);
        }

        String s3Key = fileUrl.substring("/storage/".length());

        try {
            // Xóa tệp tin từ S3
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            System.out.println(">>> DELETED FILE FROM S3: " + s3Key);
        } catch (Exception e) {
            throw new IOException("Could not delete file from S3: " + s3Key, e);
        }
    }
}