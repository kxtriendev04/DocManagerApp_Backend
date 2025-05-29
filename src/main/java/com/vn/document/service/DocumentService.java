package com.vn.document.service;

import com.vn.document.domain.Category;
import com.vn.document.domain.Document;
import com.vn.document.domain.User;
import com.vn.document.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final UserService userService;
    private final CategoryService categoryService;
    private final DocumentRepository documentRepository;

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    @Transactional
    public Document createDocument(Document document) {
        User user = userService.handleFindUserById(document.getUser().getId());
        Category category = categoryService.handleFindCategoryById(document.getCategory().getId());
        document.setUser(user);
        document.setCategory(category);
        return documentRepository.save(document);
    }

    public Document updateDocument(Long id, Document documentDetails) {
        return documentRepository.findById(id)
                .map(document -> {
                    document.setDocumentName(documentDetails.getDocumentName());
                    document.setFileType(documentDetails.getFileType());
                    document.setFileUrl(documentDetails.getFileUrl());
                    document.setPassword(documentDetails.getPassword());
                    document.setEncryptionMethod(documentDetails.getEncryptionMethod());
                    document.setCategory(documentDetails.getCategory());
                    return documentRepository.save(document);
                })
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
    public List<Document> getDocumentsByUserId(Long userId) {
        return documentRepository.findByUserId(userId);
    }
    public Document handleFindDocumentById(Long id){
        return documentRepository.findById(id).orElseThrow(()->new RuntimeException("Document id không hợp lệ"));
    }
}