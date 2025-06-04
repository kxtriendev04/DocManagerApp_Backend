package com.vn.document.repository;

import com.vn.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUserId(Long userId);
    List<Document> findByCategoryId(Long categoryId);
    Document findByFileUrl(String fileUrl);
    List<Document> findByDocumentNameContainingIgnoreCase(String documentName);

    List<Document> findByFileTypeInIgnoreCase(List<String> fileTypes);

    @Query("SELECT d FROM Document d " +
            "WHERE LOWER(d.documentName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(d.fileType) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(d.category.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Document> searchByKeyword(@Param("keyword") String keyword);

    void deleteAllByUserId(Long userId);
}