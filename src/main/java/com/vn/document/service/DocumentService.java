package com.vn.document.service;

import com.vn.document.domain.Category;
import com.vn.document.domain.Document;
import com.vn.document.domain.Permission;
import com.vn.document.domain.User;
import com.vn.document.repository.BookmarkRepository;
import com.vn.document.repository.DocumentRepository;
import com.vn.document.repository.PermissionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final UserService userService;
    private final CategoryService categoryService;
    private final DocumentRepository documentRepository;
    private final FileService fileService;
    private final PermissionRepository permissionRepository;
    private final BookmarkService bookmarkService;
    private final BookmarkRepository bookmarkRepository;
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public List<Document> getSharedDocumentsForUser(Long userId) {
        List<Permission> permissions = permissionRepository.findByUserId(userId);
        return permissions.stream()
                .map(Permission::getDocument)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional
    public Document createDocument(Document document) {
        User user = userService.handleFindUserById(document.getUser().getId());
        Category category = categoryService.handleFindCategoryById(document.getCategory().getId());
        document.setUser(user);
        document.setCategory(category);

        documentRepository.save(document);

//        if(!s2url)
//          DocumentVersion



        return documentRepository.save(document);
    }

    public List<Document> getDocumentsByCategoryId(Long categoryId) {
        return documentRepository.findByCategoryId(categoryId);
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
                    document.setIsFavorite(documentDetails.getIsFavorite()); // Cập nhật isFavorite
                    return documentRepository.save(document);
                })
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    @Transactional
    public void deleteDocument(Long id, String password) {
        permissionRepository.deleteByDocumentId(id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        if (!BCrypt.checkpw(password, document.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        try {
            fileService.deleteFile(document.getFileUrl());
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file for document id: " + id, e);
        }

        documentRepository.deleteById(id);
    }

    public List<Document> getDocumentsByUserId(Long userId) {
        return documentRepository.findByUserId(userId);
    }

    public Document handleFindDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document id không hợp lệ"));
    }

    public List<Document> searchDocumentsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tài liệu không được để trống");
        }
        return documentRepository.findByDocumentNameContainingIgnoreCase(name);
    }

    public List<Document> searchDocumentsByFileType(List<String> fileTypes) {
        if (fileTypes == null || fileTypes.isEmpty()) {
            throw new IllegalArgumentException("Định dạng file không được để trống");
        }
        return documentRepository.findByFileTypeInIgnoreCase(fileTypes);
    }

    public List<Document> searchDocumentsByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Từ khóa tìm kiếm không được để trống");
        }
        return documentRepository.searchByKeyword(keyword);
    }

    // Thêm phương thức đánh dấu/bỏ đánh dấu yêu thích
    @Transactional
    public Document toggleFavorite(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        boolean newFavoriteStatus = !document.getIsFavorite();
        document.setIsFavorite(newFavoriteStatus);
        Document updatedDocument = documentRepository.saveAndFlush(document); // Sử dụng saveAndFlush để đảm bảo commit

        if (newFavoriteStatus) {
            // Tạo bookmark khi đánh dấu yêu thích
            bookmarkService.createBookmarkForDocument(document.getUser().getId(), id);
        } else {
            // Xóa bookmark khi bỏ yêu thích
            bookmarkRepository.deleteByDocumentIdAndUserId(id, document.getUser().getId());
        }

        return updatedDocument;
    }

    // Thêm phương thức lấy danh sách tài liệu yêu thích
    public List<Document> getFavoriteDocumentsByUserId(Long userId) {
        return documentRepository.findByUserIdAndIsFavoriteTrue(userId);
    }
}