package com.vn.document.controller;

import com.vn.document.domain.Document;
import com.vn.document.domain.DocumentVersion;
import com.vn.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/shared/{userId}")
    public ResponseEntity<List<Document>> getSharedDocuments(@PathVariable Long userId) {
        List<Document> sharedDocuments = documentService.getSharedDocumentsForUser(userId);
        return ResponseEntity.ok(sharedDocuments);
    }

    @PostMapping
    public ResponseEntity<Document> createDocument(@RequestBody Document document) {
        if (document.getDocumentName() == null || document.getDocumentName().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        if (document.getFileUrl() == null || document.getFileUrl().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        if (document.getPassword() == null || document.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        if (document.getUser() == null || document.getUser().getId() == null) {
            return ResponseEntity.badRequest().body(null);
        }


        String passwordHash = BCrypt.hashpw(document.getPassword(), BCrypt.gensalt());
        document.setPassword(passwordHash);

        Document createdDocument = documentService.createDocument(document);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDocument);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(@PathVariable Long id, @RequestBody Document documentDetails) {
        if (documentDetails.getDocumentName() == null || documentDetails.getDocumentName().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        if (documentDetails.getFileUrl() == null || documentDetails.getFileUrl().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        if (documentDetails.getUser() == null || documentDetails.getUser().getId() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        if (documentDetails.getPassword() != null && !documentDetails.getPassword().isEmpty()) {
            String passwordHash = BCrypt.hashpw(documentDetails.getPassword(), BCrypt.gensalt());
            documentDetails.setPassword(passwordHash);
        }

        try {
            Document updatedDocument = documentService.updateDocument(id, documentDetails);
            return ResponseEntity.ok(updatedDocument);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Document>> getDocumentsByUserId(@PathVariable Long userId) {
        List<Document> documents = documentService.getDocumentsByUserId(userId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Document>> getDocumentsByCategoryId(@PathVariable Long categoryId) {
        List<Document> documents = documentService.getDocumentsByCategoryId(categoryId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/search/by-name")
    public ResponseEntity<List<Document>> searchDocumentsByName(@RequestParam String name) {
        try {
            List<Document> documents = documentService.searchDocumentsByName(name);
            return ResponseEntity.ok(documents);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search/by-filetype")
    public ResponseEntity<List<Document>> searchDocumentsByFileType(@RequestParam List<String> fileType) {
        try {
            List<Document> documents = documentService.searchDocumentsByFileType(fileType);
            return ResponseEntity.ok(documents);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocuments(@RequestParam String keyword) {
        try {
            List<Document> documents = documentService.searchDocumentsByKeyword(keyword);
            return ResponseEntity.ok(documents);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Thêm endpoint để đánh dấu/bỏ đánh dấu yêu thích
    @PatchMapping("/{id}/toggle-favorite")
    public ResponseEntity<Map<String, Object>> toggleFavorite(@PathVariable Long id) {
        try {
            Document updated = documentService.toggleFavorite(id);
            Map<String, Object> response = new HashMap<>();
            response.put("error", null);
            response.put("message", "Success");
            response.put("results", updated);
            response.put("status_code", 200);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("message", "Failed to toggle favorite");
            response.put("results", null);
            response.put("status_code", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // Thêm endpoint để lấy danh sách tài liệu yêu thích
    @GetMapping("/user/{userId}/favorites")
    public ResponseEntity<List<Document>> getFavoriteDocuments(@PathVariable Long userId) {
        List<Document> documents = documentService.getFavoriteDocumentsByUserId(userId);
        if (documents.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(documents);
    }
}