package com.vn.document.controller;

import com.vn.document.domain.Category;
import com.vn.document.domain.Document;
import com.vn.document.domain.User;
import com.vn.document.domain.dto.response.FileUploadResponse;
import com.vn.document.repository.CategoryRepository;
import com.vn.document.repository.DocumentRepository;
import com.vn.document.repository.DocumentVersionRepository;
import com.vn.document.domain.DocumentVersion;
import com.vn.document.repository.UserRepository;
import com.vn.document.service.CategoryService;
import com.vn.document.service.DocumentService;
import com.vn.document.service.FileService;
import jakarta.transaction.Transactional;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {
    @Autowired
    private FileService fileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DocumentService documentService;

    @PostMapping
    public ResponseEntity<Object> postFile(
            @RequestPart MultipartFile file,
            @RequestParam String folder,
            @RequestParam String password,
            @RequestParam Long userId,
            @RequestParam Long categoryId,
            @RequestParam(required = false) Long documentId) throws IOException {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Tệp không được để trống");
            }
            if (folder == null || folder.trim().isEmpty()) {
                throw new IllegalArgumentException("Thư mục không được để trống");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Mật khẩu không được để trống");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

            Category category = categoryService.getCategoryById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy category với ID: " + categoryId));

            FileUploadResponse response = fileService.handleUploadNewVersion(file, folder, password, user, category, documentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not Found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(404).body(error);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server Error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/multi")
    public ResponseEntity<List<FileUploadResponse>> postMultipleFiles(
            @RequestPart("files") MultipartFile[] files,
            @RequestParam String folder,
            @RequestParam String password,
            @RequestParam Long userId,
            @RequestParam Long categoryId,
            @RequestParam(required = false) Long documentId) throws IOException {
        try {
            if (files == null || files.length == 0) {
                throw new IllegalArgumentException("Danh sách tệp không được để trống");
            }
            if (folder == null || folder.trim().isEmpty()) {
                throw new IllegalArgumentException("Thư mục không được để trống");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Mật khẩu không được để trống");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

            Category category = categoryService.getCategoryById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy category với ID: " + categoryId));

            List<FileUploadResponse> responses = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    FileUploadResponse response = fileService.handleUploadNewVersion(file, folder, password, user, category, documentId);
                    responses.add(response);
                }
            }

            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @GetMapping
    public ResponseEntity<?> downloadFile(
            @RequestParam Long documentId,
            @RequestParam String password,
            @RequestParam(required = false) Integer versionNumber) {
        try {
            if (documentId == null) {
                throw new IllegalArgumentException("Document ID không được để trống");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Mật khẩu không được để trống");
            }

            Resource file = fileService.loadFileByVersionNumber(documentId, password, versionNumber);
            DocumentVersion version = fileService.getDocumentVersionByVersionNumber(documentId, versionNumber);
            String filename = version.getS3Url().substring(version.getS3Url().lastIndexOf("/") + 1);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(file);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            error.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/folder-size")
    public ResponseEntity<Map<String, Object>> getFolderSize(@RequestParam String folder) {
        try {
            if (folder == null || folder.trim().isEmpty()) {
                throw new IllegalArgumentException("Thư mục không được để trống");
            }

            long size = fileService.getFolderSize(folder);
            Map<String, Object> response = new HashMap<>();
            response.put("sizeInBytes", size);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new HashMap<>());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new HashMap<>());
        }
    }

    @GetMapping("/folder-sizes")
    public ResponseEntity<Map<String, Long>> getFolderSizes(@RequestParam String parent) {
        try {
            if (parent == null || parent.trim().isEmpty()) {
                throw new IllegalArgumentException("Thư mục cha không được để trống");
            }

            Map<String, Long> sizes = fileService.getFolderSizes(parent);
            return ResponseEntity.ok(sizes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new HashMap<>());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new HashMap<>());
        }
    }

    //xoá file trên s3 + CSDL
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMediaDocument(@PathVariable Long id, @RequestParam String password) {
        try {
            documentService.deleteMediaDocument(id, password);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            if (e.getMessage().contains("Mật khẩu không hợp lệ")) {
                error.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
                error.put("message", "Mật khẩu không hợp lệ");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            error.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
            error.put("message", "Không tìm thấy document: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            error.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    //Xoá link trong CSDL
    @DeleteMapping("/link/{id}")
    public ResponseEntity<?> deleteLinkDocument(@PathVariable Long id, @RequestParam String password) {
        try {
            documentService.deleteLinkDocument(id, password);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            if (e.getMessage().contains("Mật khẩu không hợp lệ")) {
                error.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
                error.put("message", "Mật khẩu không hợp lệ");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            error.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
            error.put("message", "Không tìm thấy document: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            error.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}