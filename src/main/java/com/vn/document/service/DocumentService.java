package com.vn.document.service;

import com.vn.document.domain.*;
import com.vn.document.repository.*;
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
    private final DocumentVersionRepository documentVersionRepository;
    private final AccessLogRepository accessLogRepository;
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
    public void deleteMediaDocument(Long id, String password) {
        // Tìm document
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy document với ID: " + id));

        // Kiểm tra mật khẩu
        if (!BCrypt.checkpw(password, document.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu không hợp lệ");
        }

        // Kiểm tra loại document
//        if (!"media".equalsIgnoreCase(document.getFileType())) {
//            throw new IllegalArgumentException("Document không phải loại media");
//        }

        try {
            // Xóa file trên S3
            List<DocumentVersion> versions = documentVersionRepository.findByDocumentId(id);
            for (DocumentVersion version : versions) {
                String s3Url = version.getS3Url();
                if (s3Url != null && s3Url.startsWith("/storage/")) {
                    fileService.deleteFile(s3Url);
                }
            }

            // Xóa các bản ghi liên quan
            documentVersionRepository.deleteByDocumentId(id);
            permissionRepository.deleteByDocumentId(id);
            bookmarkRepository.deleteByDocumentId(id);
            accessLogRepository.deleteByDocumentId(id);

            // Xóa document
            documentRepository.deleteById(id);

        } catch (IOException e) {
            throw new RuntimeException("Không thể xóa file trên S3: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi server khi xóa document: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteLinkDocument(Long id, String password) {
        // Tìm document
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy document với ID: " + id));

        // Kiểm tra mật khẩu
        if (!BCrypt.checkpw(password, document.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu không hợp lệ");
        }

        // Kiểm tra loại document
        if (!"link".equalsIgnoreCase(document.getFileType())) {
            throw new IllegalArgumentException("Document không phải loại link");
        }

        try {
            // Xóa các bản ghi liên quan
            documentVersionRepository.deleteByDocumentId(id);
            permissionRepository.deleteByDocumentId(id);
            bookmarkRepository.deleteByDocumentId(id);
            accessLogRepository.deleteByDocumentId(id);

            // Xóa document
            documentRepository.deleteById(id);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi server khi xóa document: " + e.getMessage(), e);
        }
    }

    public List<Document> getDocumentsByUserId(Long userId) {
        List<Document> documents = documentRepository.findByUserId(userId);
        documents.forEach(doc -> {
            List<Bookmark> bookmarks = bookmarkRepository.findByDocumentIdAndUserId(doc.getId(), userId);
            doc.setIsFavorite(!bookmarks.isEmpty() && bookmarks.get(0).getIsFavorite());
        });
        return documents;
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