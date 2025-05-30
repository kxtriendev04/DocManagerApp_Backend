package com.vn.document.repository;

import com.vn.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUserId(Long userId);
    List<Document> findByCategoryId(Long categoryId);
    Document findByFileUrl(String fileUrl);
    List<Document> findByDocumentNameContainingIgnoreCase(String documentName);

    List<Document> findByFileTypeInIgnoreCase(List<String> fileTypes);

}