package com.vn.document.controller;

import com.vn.document.domain.Document;
import com.vn.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public List<Document> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Document> createDocument(@RequestBody Document document) {
        Document createdDocument = documentService.createDocument(document);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDocument);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(@PathVariable Long id, @RequestBody Document documentDetails) {
        try {
            Document updatedDocument = documentService.updateDocument(id, documentDetails);
            return ResponseEntity.ok(updatedDocument);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Document>> getDocumentsByUserId(@PathVariable Long userId) {
        List<Document> documents = documentService.getDocumentsByUserId(userId);
        return ResponseEntity.ok(documents);
    }
}
