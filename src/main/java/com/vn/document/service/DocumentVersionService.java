package com.vn.document.service;

import com.vn.document.domain.Document;
import com.vn.document.domain.DocumentVersion;
import com.vn.document.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentVersionService {
    private final DocumentService documentService;
    private final DocumentVersionRepository documentVersionRepository;

    // Lấy tất cả các phiên bản của tài liệu theo docId
    public List<DocumentVersion> getVersionsByDocumentId(Long docId) {

        return documentVersionRepository.findByDocumentId(docId);
    }

    // Tạo một phiên bản tài liệu mới
    public DocumentVersion createVersion(DocumentVersion version) {
        Document document = documentService.handleFindDocumentById(version.getDocument().getId());
        version.setDocument(document);
        return documentVersionRepository.save(version);
    }
}
