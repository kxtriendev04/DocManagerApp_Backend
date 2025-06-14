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
    List<Document> findByUserIdAndIsFavoriteTrue(Long userId); // Thêm phương thức tìm tài liệu yêu thích

    @Query("SELECT d FROM Document d " +
            "WHERE (d.user.id = :userId OR d.id IN (SELECT p.document.id FROM Permission p WHERE p.user.id = :userId)) " +
            "AND LOWER(d.documentName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Document> findByUserIdOrSharedAndDocumentNameContainingIgnoreCase(@Param("userId") Long userId, @Param("name") String name);

    @Query("SELECT d FROM Document d " +
            "WHERE (d.user.id = :userId OR d.id IN (SELECT p.document.id FROM Permission p WHERE p.user.id = :userId)) " +
            "AND d.fileType IN :fileTypes")
    List<Document> findByUserIdOrSharedAndFileTypeInIgnoreCase(@Param("userId") Long userId, @Param("fileTypes") List<String> fileTypes);

    @Query("SELECT d FROM Document d " +
            "WHERE (d.user.id = :userId OR d.id IN (SELECT p.document.id FROM Permission p WHERE p.user.id = :userId)) " +
            "AND (LOWER(d.documentName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(d.fileType) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(d.category.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Document> searchByKeywordForUserOrShared(@Param("userId") Long userId, @Param("keyword") String keyword);

    void deleteAllByUserId(Long userId);
}