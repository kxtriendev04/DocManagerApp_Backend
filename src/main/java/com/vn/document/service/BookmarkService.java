package com.vn.document.service;

import com.vn.document.domain.Bookmark;
import com.vn.document.domain.Document;
import com.vn.document.domain.User;
import com.vn.document.repository.BookmarkRepository;
import com.vn.document.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final UserService userService;
    private final DocumentRepository documentRepository;
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
    @Transactional
    public Bookmark createBookmark(Bookmark bookmark) {
        User user = userService.handleFindUserById(bookmark.getUser().getId());
        Document document = documentRepository.findById(bookmark.getDocument().getId())
                .orElseThrow(() -> new RuntimeException("Document not found"));
        if (document.getCategory() == null) {
            throw new IllegalArgumentException("Document must have a category");
        }
        bookmark.setUser(user);
        bookmark.setDocument(document);
        bookmark.setCategory(document.getCategory());
        bookmark.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return bookmarkRepository.save(bookmark);
    }

    // Xóa một bookmark
    public void deleteBookmark(Long bookmarkId) {
        bookmarkRepository.deleteById(bookmarkId);
    }

    public void deleteBookmarkByDocumentIdAndUserId(Long documentId, Long userId) {
        bookmarkRepository.deleteByDocumentIdAndUserId(documentId, userId);
    }

    public List<Bookmark> getBookmarksByUserId(Long userId, String sortBy, String sortDir) {
        List<String> validSortFields = Arrays.asList("id", "createdAt", "note", "isFavorite");
        if (!validSortFields.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        return bookmarkRepository.findByUserId(userId, sort);
    }

    public List<Bookmark> searchBookmarks(Long userId, String keyword) {
        return bookmarkRepository.findByUserIdAndDocument_DocumentNameContainingIgnoreCaseOrDocument_EncryptionMethodContainingIgnoreCase(
                userId, keyword, keyword
        );
    }

    @Transactional
    public Bookmark createBookmarkForDocument(Long userId, Long documentId) {
        User user = userService.handleFindUserById(userId);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        if (document.getCategory() == null) {
            throw new IllegalArgumentException("Document must have a category");
        }
        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setDocument(document);
        bookmark.setCategory(document.getCategory());
        bookmark.setIsFavorite(true);
        bookmark.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return bookmarkRepository.save(bookmark);
    }

    @Transactional
    public Bookmark toggleFavorite(Long id) {
        Bookmark bookmark = bookmarkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));
        boolean newFavoriteStatus = !bookmark.getIsFavorite();
        bookmark.setIsFavorite(newFavoriteStatus);

        // Đồng bộ trạng thái isFavorite của tài liệu
        Document document = documentRepository.findById(bookmark.getDocument().getId())
                .orElseThrow(() -> new RuntimeException("Document not found"));
        document.setIsFavorite(newFavoriteStatus);
        documentRepository.saveAndFlush(document);

        if (!newFavoriteStatus) {
            // Xóa bookmark khi bỏ yêu thích
            bookmarkRepository.delete(bookmark);
            return null; // Trả về null để báo hiệu bookmark đã bị xóa
        }

        return bookmarkRepository.save(bookmark);
    }

    // Cập nhật bookmark
    public Bookmark updateBookmark(Long id, Bookmark updatedBookmark) {
        Bookmark bookmark = bookmarkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bookmark"));
        if (updatedBookmark.getDocument() != null && updatedBookmark.getDocument().getCategory() != null) {
            bookmark.setCategory(updatedBookmark.getDocument().getCategory());
        }
        return bookmarkRepository.save(bookmark);
    }

    // Lấy chi tiết bookmark
    public Bookmark getBookmarkById(Long id) {
        return bookmarkRepository.findById(id).orElse(null);
    }

//    public Bookmark moveToCollection(Long id, Long newCollectionId) {
//        Bookmark bookmark = bookmarkRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy bookmark"));
//        // Cập nhật logic di chuyển collection nếu cần
//        return bookmarkRepository.save(bookmark);
//    }
}