package com.vn.document.repository;

import com.vn.document.domain.DocumentVersion;
import lombok.Getter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    // Tìm tất cả các phiên bản của tài liệu theo docId
    List<DocumentVersion> findByDocumentId(Long docId);

    @Query("SELECT MAX(v.versionNumber) FROM DocumentVersion v WHERE v.document.id = :documentId")
    Integer findMaxVersionByDocumentId(Long documentId);

    @Query("SELECT v FROM DocumentVersion v WHERE v.document.id = :documentId AND v.versionNumber = (SELECT MAX(v2.versionNumber) FROM DocumentVersion v2 WHERE v2.document.id = :documentId)")
    Optional<DocumentVersion> findLatestVersionByDocumentId(Long documentId);

}
