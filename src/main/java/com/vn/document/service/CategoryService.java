package com.vn.document.service;

import com.vn.document.domain.Category;
import com.vn.document.domain.User;
import com.vn.document.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final UserService userService;
    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public Category createCategory(Category category) {
        User user = userService.handleFindUserById(category.getUser().getId());
        category.setUser(user);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long categoryId, Category categoryDetails) {
        return categoryRepository.findById(categoryId)
                .map(category -> {
                    category.setCategoryName(categoryDetails.getCategoryName());
                    category.setUpdatedAt(categoryDetails.getUpdatedAt());
                    return categoryRepository.save(category);
                })
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
    public Category handleFindCategoryById(Long id){
        return categoryRepository.findById(id).orElseThrow(()->(new RuntimeException("Category id không hợp lệ!")));
    }
    public List<Category> handleFindCategoryByUserId(Long userId){
        return categoryRepository.findByUserId(userId);
    }


    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}