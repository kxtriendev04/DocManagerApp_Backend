package com.vn.document.repository;

import com.vn.document.domain.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    // Tìm tất cả các phiên bản của tài liệu theo docId
    List<DocumentVersion> findByDocumentId(Long docId);
}
