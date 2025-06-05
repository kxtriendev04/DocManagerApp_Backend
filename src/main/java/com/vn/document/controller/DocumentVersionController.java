package com.vn.document.controller;

import com.vn.document.domain.DocumentVersion;
import com.vn.document.service.DocumentVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/document-versions")
@RequiredArgsConstructor
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;

    // API để lấy tất cả các phiên bản của tài liệu theo docId
    @GetMapping("/document/{docId}")
    public ResponseEntity<List<DocumentVersion>> getVersionsByDocumentId(@PathVariable Long docId) {
        List<DocumentVersion> versions = documentVersionService.getVersionsByDocumentId(docId);
        if (versions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(versions);
    }

    // API để tạo một phiên bản tài liệu mới
    @PostMapping
    public ResponseEntity<DocumentVersion> createDocumentVersion(@RequestBody DocumentVersion version) {
        DocumentVersion createdVersion = documentVersionService.createVersion(version);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVersion);
    }
//    Get DocumentByDocumentById
}
