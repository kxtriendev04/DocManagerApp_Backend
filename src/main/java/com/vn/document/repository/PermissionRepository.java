package com.vn.document.repository;

import com.vn.document.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // Tìm quyền của người dùng theo userId
    List<Permission> findByUserId(Long userId);

    // Tìm quyền của tài liệu theo docId
    List<Permission> findByDocumentId(Long docId);

    Optional<Permission> findByUserIdAndDocumentId(Long userId, Long documentId);

    void deleteByDocumentId(Long documentId);

}
