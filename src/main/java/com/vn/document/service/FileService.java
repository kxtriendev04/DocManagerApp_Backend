package com.vn.document.service;

import com.vn.document.domain.Category;
import com.vn.document.domain.Document;
import com.vn.document.domain.DocumentVersion;
import com.vn.document.domain.User;
import com.vn.document.domain.dto.response.FileUploadResponse;
import com.vn.document.repository.DocumentRepository;
import com.vn.document.repository.DocumentVersionRepository;
import com.vn.document.util.AESUtil;
import lombok.Getter;
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

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.time.Duration;

import java.io.IOException;

@Service
@Getter
public class FileService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private WaterMarkService waterMarkService;

    @Autowired
    private DocumentVersionRepository documentVersionRepository;

    public FileUploadResponse handleUploadNewVersion(
            MultipartFile file, String folder, String password, User user, Category category, Long documentId
    ) throws IOException {
        String finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
        String s3Key = folder + "/" + finalName;

        try {
            // Kiểm tra định dạng tệp
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String watermarkText = "Watermark";
            MultipartFile watermarkedFile = file; // Default to original file if no watermarking is applied

            // Kiểm tra loại tệp và gọi hàm tương ứng
            if (fileExtension.equalsIgnoreCase("pdf")) {
                watermarkedFile = waterMarkService.addWatermarkToPDF(file, watermarkText);
            } else if (fileExtension.equalsIgnoreCase("docx")) {
                watermarkedFile = waterMarkService.addWatermarkToDocx(file, watermarkText);
            } else if (isImage(fileExtension)) {
                watermarkedFile = waterMarkService.addWatermarkToImage(file, watermarkText);
            } else if (isVideo(fileExtension)) {
                watermarkedFile = waterMarkService.addWatermarkToVideo(file, watermarkText);
            }

            // Mã hóa
            byte[] originalBytes = watermarkedFile.getBytes();
            byte[] encryptedBytes = AESUtil.encrypt(originalBytes, password);

            // Lưu tệp tin mã hóa lên S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(encryptedBytes));

            long fileSize = encryptedBytes.length;
            String s3Url = "/storage/" + folder + "/" + finalName;

            Document document;
            int newVersionNumber;

            if (documentId == null) {
                // Tạo mới Document
                document = new Document();
                document.setUser(user);
                document.setCategory(category);
                document.setDocumentName(file.getOriginalFilename());
//                if (fileExtension.equalsIgnoreCase("pdf")) {
//                    watermarkedFile = waterMarkService.addWatermarkToPDF(file, watermarkText);
//                } else if (fileExtension.equalsIgnoreCase("docx")) {
//                    watermarkedFile = waterMarkService.addWatermarkToDocx(file, watermarkText);
//                } else if (isImage(fileExtension)) {
//                    watermarkedFile = waterMarkService.addWatermarkToImage(file, watermarkText);
//                } else if (isVideo(fileExtension)) {
//                    watermarkedFile = waterMarkService.addWatermarkToVideo(file, watermarkText);
//                }
                if(isImage(fileExtension))
                    document.setFileType("Image");
                else if(isVideo(fileExtension))
                    document.setFileType("Video");
                else
                    document.setFileType("Document");
                if (s3Url != null && !s3Url.startsWith("/storage/")) {
                    document.setFileUrl(s3Url);
                }
                document.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                document.setEncryptionMethod("AES");
                Timestamp now = new Timestamp(System.currentTimeMillis());
                document.setCreatedAt(now);
                document.setUpdatedAt(now);
                document.setIsFavorite(false);
                documentRepository.save(document);

                newVersionNumber = 1;
            } else {
                // Lấy Document đã tồn tại
                document = documentRepository.findById(documentId)
                        .orElseThrow(() -> new RuntimeException("Document không tồn tại"));

                // Lấy versionNumber max hiện tại
                Integer maxVersion = documentVersionRepository.findMaxVersionByDocumentId(documentId);
                newVersionNumber = (maxVersion == null ? 0 : maxVersion) + 1;
            }

            // Tạo version mới
            DocumentVersion newVersion = new DocumentVersion();
            newVersion.setDocument(document);
            newVersion.setVersionNumber(newVersionNumber);
            newVersion.setS3Url(s3Url);
            newVersion.setFileSize((Long) fileSize);   //đổi từ int sang Long
            documentVersionRepository.save(newVersion);

            // Trả về response
            return new FileUploadResponse(s3Url, fileSize, document.getId(), newVersion.getId());
        } catch (Exception e) {
            e.printStackTrace(); // Thêm dòng này để in chi tiết stacktrace
            throw new IOException("Encryption or S3 upload failed: " + e.getMessage(), e);
        }
    }

    public Map<String, String> uploadFileToS3(MultipartFile multipartFile) throws IOException {
        try {
            // Chuyển MultipartFile thành File tạm
            File file = convertMultiPartToFile(multipartFile);
            String originalFilename = multipartFile.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = "temp/" + UUID.randomUUID().toString() + fileExtension;

            // Tải file lên S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFileName)
                    .build();
            s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromFile(file));

            // Xóa file tạm
            file.delete();

            // Tạo pre-signed URL (hết hạn sau 1 giờ)
            String fileUrl = s3Client.utilities().getUrl(builder -> builder
                    .bucket(bucketName)
                    .key(uniqueFileName)
//                    .expiration(Duration.ofHours(1))
            ).toExternalForm();

            // Trả về thông tin file
            Map<String, String> fileInfo = new HashMap<>();
            fileInfo.put("fileName", uniqueFileName);
            fileInfo.put("fileUrl", fileUrl);
            return fileInfo;
        } catch (Exception e) {
            throw new IOException("Không thể tải file lên S3: " + e.getMessage(), e);
        }
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }

    public void deleteFileFromS3(String fileName) throws IOException {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            System.out.println(">>> DELETED FILE FROM S3: " + fileName);
        } catch (Exception e) {
            throw new IOException("Không thể xóa file trên S3: " + fileName, e);
        }
    }

    public DocumentVersion getDocumentVersion(Long documentId, Long versionId) throws IOException {
        // Kiểm tra document tồn tại
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IOException("Không tìm thấy document với ID: " + documentId));

        // Lấy version
        if (versionId != null) {
            DocumentVersion version = documentVersionRepository.findById(versionId)
                    .orElseThrow(() -> new IOException("Không tìm thấy version với ID: " + versionId));
            if (!version.getDocument().getId().equals(document.getId())) {
                throw new IOException("Version không thuộc về document được chỉ định");
            }
            return version;
        } else {
            DocumentVersion latestVersion = documentVersionRepository.findLatestVersionByDocumentId(document.getId())
                    .orElseThrow(() -> new IOException("Không tìm thấy version nào cho document: " + document.getId()));
            return latestVersion;
        }
    }

    public Resource loadFileByVersionNumber(Long documentId, String password, Integer versionNumber) throws IOException {
        // Tìm document
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IOException("Không tìm thấy document với ID: " + documentId));

        // Kiểm tra mật khẩu
        if (!BCrypt.checkpw(password, document.getPassword())) {
            throw new IOException("Mật khẩu không hợp lệ");
        }

        // Tìm version
        String s3Key;
        DocumentVersion version;
        if (versionNumber != null) {
            version = documentVersionRepository.findByDocumentIdAndVersionNumber(documentId, versionNumber)
                    .orElseThrow(() -> new IOException("Không tìm thấy version với số: " + versionNumber));
            s3Key = version.getS3Url().substring("/storage/".length());
        } else {
            version = documentVersionRepository.findLatestVersionByDocumentId(documentId)
                    .orElseThrow(() -> new IOException("Không tìm thấy version nào cho document: " + documentId));
            s3Key = version.getS3Url().substring("/storage/".length());
        }

        try {
            // Tải file từ S3
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            byte[] encryptedBytes = s3Client.getObject(getObjectRequest).readAllBytes();

            // Giải mã file
            byte[] decryptedBytes = AESUtil.decrypt(encryptedBytes, password);
            return new ByteArrayResource(decryptedBytes);
        } catch (Exception e) {
            throw new IOException("Không thể tải hoặc giải mã file từ S3: " + e.getMessage(), e);
        }
    }

    public DocumentVersion getDocumentVersionByVersionNumber(Long documentId, Integer versionNumber) throws IOException {
        // Kiểm tra document tồn tại
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IOException("Không tìm thấy document với ID: " + documentId));

        // Lấy version
        if (versionNumber != null) {
            return documentVersionRepository.findByDocumentIdAndVersionNumber(documentId, versionNumber)
                    .orElseThrow(() -> new IOException("Không tìm thấy version với số: " + versionNumber));
        } else {
            return documentVersionRepository.findLatestVersionByDocumentId(documentId)
                    .orElseThrow(() -> new IOException("Không tìm thấy version nào cho document: " + documentId));
        }
    }

    public long getFolderSize(String folderPrefix) {
        long totalSize = 0;

        String continuationToken = null;
        do {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(folderPrefix)
                    .maxKeys(1000);

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

    public Resource loadFile(Long documentId, String password, Long versionId) throws IOException {
        System.out.println("Loading file for documentId: " + documentId + ", versionId: " + versionId);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IOException("Document not found for ID: " + documentId));

        System.out.println("Document found, password hash: " + document.getPassword());
        try {
            if (!BCrypt.checkpw(password, document.getPassword())) {
                throw new IOException("Invalid password");
            }
        } catch (IllegalArgumentException e) {
            System.err.println("BCrypt error: " + e.getMessage());
            throw new IOException("Invalid password hash format", e);
        }

        String s3Key;
        if (versionId != null) {
            DocumentVersion version = documentVersionRepository.findById(versionId)
                    .orElseThrow(() -> new IOException("Version not found for ID: " + versionId));
            if (!version.getDocument().getId().equals(document.getId())) {
                throw new IOException("Version does not belong to the specified document");
            }
            s3Key = version.getS3Url().substring("/storage/".length());
        } else {
            DocumentVersion latestVersion = documentVersionRepository.findLatestVersionByDocumentId(document.getId())
                    .orElseThrow(() -> new IOException("No versions found for document: " + document.getId()));
            s3Key = latestVersion.getS3Url().substring("/storage/".length());
        }
        System.out.println("Downloading from S3, key: " + s3Key);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            byte[] encryptedBytes = s3Client.getObject(getObjectRequest).readAllBytes();

            byte[] decryptedBytes = AESUtil.decrypt(encryptedBytes, password);
            Resource resource = new ByteArrayResource(decryptedBytes);
            System.out.println("Returning resource: " + resource.getClass().getName());
            return resource;
        } catch (Exception e) {
            throw new IOException("Could not decrypt or download file from S3 for documentId: " + documentId, e);
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

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    private boolean isImage(String fileExtension) {
        return fileExtension.equalsIgnoreCase("jpg") ||
                fileExtension.equalsIgnoreCase("jpeg") ||
                fileExtension.equalsIgnoreCase("png") ||
                fileExtension.equalsIgnoreCase("gif") ||
                fileExtension.equalsIgnoreCase("bmp") ||
                fileExtension.equalsIgnoreCase("webp");
    }

    private boolean isVideo(String fileExtension) {
        return fileExtension.equalsIgnoreCase("mp4") ||
                fileExtension.equalsIgnoreCase("avi") ||
                fileExtension.equalsIgnoreCase("mkv") ||
                fileExtension.equalsIgnoreCase("mov") ||
                fileExtension.equalsIgnoreCase("flv") ||
                fileExtension.equalsIgnoreCase("wmv");
    }
}