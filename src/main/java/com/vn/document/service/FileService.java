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
import software.amazon.awssdk.services.s3.model.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import java.io.File;
import java.io.IOException;

@Service
public class FileService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private WaterMarkService waterMarkService;

    public String handleStoreFile(MultipartFile file, String folder, String password) throws IOException {
        String finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
        String s3Key = folder + "/" + finalName; // Tạo key cho S3 (e.g., "folder/filename")

        try {
            // Kiểm tra định dạng tệp
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String watermarkText = "Watermark";
            MultipartFile watermarkedFile = null;
            // Kiểm tra loại tệp và gọi hàm tương ứng
            if (fileExtension.equalsIgnoreCase("pdf")) {
                // Xử lý watermark cho PDF
                watermarkedFile = waterMarkService.addWatermarkToPDF(file, watermarkText);
            } else if (fileExtension.equalsIgnoreCase("docx")) {
                // Xử lý watermark cho DOCX
                watermarkedFile = waterMarkService.addWatermarkToDocx(file, watermarkText);
            } else if (isImage(fileExtension)) {
                // Xử lý watermark cho ảnh
                watermarkedFile = waterMarkService.addWatermarkToImage(file, watermarkText);
            } else if (isVideo(fileExtension)) {
                // Xử lý watermark cho video
                watermarkedFile = waterMarkService.addWatermarkToVideo(file, watermarkText);
            }


//          Mã hóa
            byte[] originalBytes = watermarkedFile.getBytes();
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

    public long getFolderSize(String folderPrefix) {
        long totalSize = 0;

        String continuationToken = null;
        do {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(folderPrefix)
                    .maxKeys(1000); // optional

            if (continuationToken != null) {
                requestBuilder.continuationToken(continuationToken);
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());
            for (S3Object s3Object : response.contents()) {
                totalSize += s3Object.size();
            }

            continuationToken = response.nextContinuationToken();
        } while (continuationToken != null);

        return totalSize;
    }

    public Map<String, Long> getFolderSizes(String parentFolder) {
        Map<String, Long> folderSizes = new HashMap<>();

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(parentFolder)
                .delimiter("/")
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        List<CommonPrefix> subFolders = response.commonPrefixes();
        for (CommonPrefix prefix : subFolders) {
            String folderPrefix = prefix.prefix();

            long folderSize = 0;
            String continuationToken = null;

            do {
                ListObjectsV2Request subRequest = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(folderPrefix)
                        .continuationToken(continuationToken)
                        .build();

                ListObjectsV2Response subResponse = s3Client.listObjectsV2(subRequest);
                for (S3Object s3Object : subResponse.contents()) {
                    folderSize += s3Object.size();
                }

                continuationToken = subResponse.nextContinuationToken();
            } while (continuationToken != null);

            folderSizes.put(folderPrefix, folderSize);
        }

        return folderSizes;
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
    // Phương thức lấy phần mở rộng của file
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1).toLowerCase(); // Trả về phần mở rộng file (nhỏ chữ)
        }
        return "";
    }

    // Phương thức kiểm tra file có phải là hình ảnh hay không
    private boolean isImage(String fileExtension) {
        return fileExtension.equalsIgnoreCase("jpg") ||
                fileExtension.equalsIgnoreCase("jpeg") ||
                fileExtension.equalsIgnoreCase("png") ||
                fileExtension.equalsIgnoreCase("gif") ||
                fileExtension.equalsIgnoreCase("bmp") ||
                fileExtension.equalsIgnoreCase("webp");
    }

    // Phương thức kiểm tra file có phải là video hay không
    private boolean isVideo(String fileExtension) {
        return fileExtension.equalsIgnoreCase("mp4") ||
                fileExtension.equalsIgnoreCase("avi") ||
                fileExtension.equalsIgnoreCase("mkv") ||
                fileExtension.equalsIgnoreCase("mov") ||
                fileExtension.equalsIgnoreCase("flv") ||
                fileExtension.equalsIgnoreCase("wmv");
    }
}