package com.vn.document.repository;

import com.vn.document.domain.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    // Tìm tất cả nhật ký truy cập của người dùng theo userId
    List<AccessLog> findByUserId(Long userId);

    // Tìm tất cả nhật ký truy cập của tài liệu theo docId
    List<AccessLog> findByDocumentId(Long docId);

    void deleteAllByUserId(Long userId);

    void deleteByDocumentId(Long id);
}
