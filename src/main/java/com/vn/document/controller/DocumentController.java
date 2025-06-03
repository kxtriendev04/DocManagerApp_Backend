package com.vn.document.controller;

import com.vn.document.domain.Document;
import com.vn.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        // Kiểm tra dữ liệu đầu vào
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

        // Băm mật khẩu trước khi lưu
        String passwordHash = BCrypt.hashpw(document.getPassword(), BCrypt.gensalt());
        document.setPassword(passwordHash);

        Document createdDocument = documentService.createDocument(document);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDocument);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(@PathVariable Long id, @RequestBody Document documentDetails) {
        // Kiểm tra dữ liệu đầu vào
        if (documentDetails.getDocumentName() == null || documentDetails.getDocumentName().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        if (documentDetails.getFileUrl() == null || documentDetails.getFileUrl().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        if (documentDetails.getUser() == null || documentDetails.getUser().getId() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        // Nếu có mật khẩu mới, băm lại
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, @RequestParam String password) {
        try {
            documentService.deleteDocument(id, password);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Invalid password")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Document>> getDocumentsByUserId(@PathVariable Long userId) {
        List<Document> documents = documentService.getDocumentsByUserId(userId);
        return ResponseEntity.ok(documents);
    }
    @GetMapping("/category/{categoryId}")
    public List<Document> getDocumentsByCategoryId(@PathVariable Long categoryId) {
        return documentService.getDocumentsByCategoryId(categoryId);
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


}