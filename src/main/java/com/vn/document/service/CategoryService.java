package com.vn.document.service;

import com.vn.document.domain.Category;
import com.vn.document.domain.CategoryGroup;
import com.vn.document.domain.Document;
import com.vn.document.domain.User;
import com.vn.document.repository.CategoryRepository;
import com.vn.document.repository.DocumentRepository;
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
    private final DocumentRepository documentRepository; // Inject DocumentRepository
    private final FileService fileService; // Inject FileService

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

        // Xóa từng Document và các file liên quan trên S3
        for (Document document : documents) {
            // Xóa file trên S3
            if (document.getFileUrl() != null && document.getFileUrl().startsWith("/storage/")) {
                try {
                    fileService.deleteFile(document.getFileUrl());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete file from S3 for document ID: " + document.getId(), e);
                }
            }
            // Xóa Document (các DocumentVersion liên quan sẽ được xóa tự động nếu có cascade)
            documentRepository.delete(document);
        }

        // Xóa category
        categoryRepository.deleteById(categoryId);
    }
}