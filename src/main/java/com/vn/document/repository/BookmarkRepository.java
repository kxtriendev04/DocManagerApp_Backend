package com.vn.document.repository;

import com.vn.document.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // Tìm kiếm các bookmark của người dùng theo userId
    List<Bookmark> findByUserId(Long userId);

    // Tìm kiếm các bookmark của tài liệu theo docId
    List<Bookmark> findByDocumentId(Long docId);
}
