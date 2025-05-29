package com.vn.document.service;

import com.vn.document.domain.Bookmark;
import com.vn.document.domain.Document;
import com.vn.document.domain.User;
import com.vn.document.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final UserService userService;
    private final DocumentService documentService;
    private final BookmarkRepository bookmarkRepository;

    // Lấy tất cả các bookmark của người dùng
    public List<Bookmark> getBookmarksByUserId(Long userId) {
        return bookmarkRepository.findByUserId(userId);
    }

    // Lấy tất cả các bookmark của tài liệu
    public List<Bookmark> getBookmarksByDocId(Long docId) {
        return bookmarkRepository.findByDocumentId(docId);
    }

    // Tạo một bookmark mới
    public Bookmark createBookmark(Bookmark bookmark) {
        User user = userService.handleFindUserById(bookmark.getUser().getId());
        Document document = documentService.handleFindDocumentById(bookmark.getDocument().getId());
        bookmark.setUser(user);
        bookmark.setDocument(document);
        return bookmarkRepository.save(bookmark);
    }

    // Xóa một bookmark
    public void deleteBookmark(Long bookmarkId) {
        bookmarkRepository.deleteById(bookmarkId);
    }
}
