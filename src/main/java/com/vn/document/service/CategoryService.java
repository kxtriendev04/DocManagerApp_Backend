package com.vn.document.service;

import com.vn.document.domain.Category;
import com.vn.document.domain.CategoryGroup;
import com.vn.document.domain.Document;
import com.vn.document.domain.User;
import com.vn.document.repository.CategoryRepository;
import com.vn.document.repository.DocumentRepository;
import com.vn.document.repository.BookmarkRepository; // Repository cho bookmarks
import com.vn.document.repository.DocumentVersionRepository; // Repository cho document_versions
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final DocumentRepository documentRepository;
    private final BookmarkRepository bookmarkRepository; // Inject BookmarkRepository
    private final DocumentVersionRepository documentVersionRepository; // Inject DocumentVersionRepository
    private final FileService fileService;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public Category createCategory(Category category) {
        User user = userService.handleFindUserById(category.getUser().getId());
        category.setUser(user);
        if (category.getCategoryGroup() == null) {
            category.setCategoryGroup(CategoryGroup.MAIN_BOOSTER);
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long categoryId, Category categoryDetails) {
        return categoryRepository.findById(categoryId)
                .map(category -> {
                    category.setCategoryName(categoryDetails.getCategoryName());
                    category.setCategoryGroup(categoryDetails.getCategoryGroup() != null
                            ? categoryDetails.getCategoryGroup()
                            : CategoryGroup.MAIN_BOOSTER);
                    category.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                    return categoryRepository.save(category);
                })
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public Category handleFindCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category id không hợp lệ!"));
    }

    public List<Category> handleFindCategoryByUserId(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        // Kiểm tra xem category có tồn tại không
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

        // Tìm tất cả Document liên quan đến category
        List<Document> documents = documentRepository.findByCategoryId(categoryId);

        // Xóa từng Document và các bản ghi liên quan
        for (Document document : documents) {
            // Xóa các bookmark liên quan
            bookmarkRepository.deleteByDocumentId(document.getId());

            // Xóa các document_versions liên quan
            documentVersionRepository.deleteByDocumentId(document.getId());

            // Xóa file trên S3 nếu có
            if (document.getFileUrl() != null && document.getFileUrl().startsWith("/storage/")) {
                try {
                    fileService.deleteFile(document.getFileUrl());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete file from S3 for document ID: " + document.getId(), e);
                }
            }

            // Xóa Document
            documentRepository.delete(document);
        }

        // Xóa category
        categoryRepository.deleteById(categoryId);
    }
}