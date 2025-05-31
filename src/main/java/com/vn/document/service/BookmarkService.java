package com.vn.document.service;

import com.vn.document.domain.Bookmark;
import com.vn.document.domain.Document;
import com.vn.document.domain.User;
import com.vn.document.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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

    // Lấy bookmark của người dùng với sắp xếp
    public List<Bookmark> getBookmarksByUserId(Long userId, String sortBy, String sortDir) {
        // Kiểm tra sortBy hợp lệ
        List<String> validSortFields = Arrays.asList("id", "createdAt", "note", "isFavorite");
        if (!validSortFields.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        return bookmarkRepository.findByUserId(userId, sort);
    }

    // Tìm kiếm bookmark
    public List<Bookmark> searchBookmarks(Long userId, String keyword) {
        return bookmarkRepository.findByUserIdAndDocument_DocumentNameContainingIgnoreCaseOrDocument_EncryptionMethodContainingIgnoreCase(
                userId, keyword, keyword
        );
    }

    // Đánh dấu yêu thích
    public Bookmark toggleFavorite(Long id) {
        Bookmark bookmark = bookmarkRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        bookmark.setIsFavorite(!bookmark.getIsFavorite());
        return bookmarkRepository.save(bookmark);
    }

    // Cập nhật bookmark
    public Bookmark updateBookmark(Long id, Bookmark updatedBookmark) {
        Bookmark bookmark = bookmarkRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        bookmark.setDocument(updatedBookmark.getDocument()); // hoặc chỉ cập nhật vài trường
        // Có thể cập nhật thêm category, note nếu cần
        return bookmarkRepository.save(bookmark);
    }

    // Lấy chi tiết bookmark
    public Bookmark getBookmarkById(Long id) {
        return bookmarkRepository.findById(id).orElse(null);
    }

    // Di chuyển bookmark đến collection
    public Bookmark moveToCollection(Long id, Long newCollectionId) {
        Bookmark bookmark = bookmarkRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        return bookmarkRepository.save(bookmark);
    }

//
//    public List<Bookmark> filterBookmarks(Long userId, Long categoryId, Long collectionId) {
//        if (categoryId != null && collectionId != null) {
//            return bookmarkRepository.findByUserIdAndCategoryIdAndCollectionId(userId, categoryId, collectionId);
//        } else if (categoryId != null) {
//            return bookmarkRepository.findByUserIdAndCategoryId(userId, categoryId);
//        } else if (collectionId != null) {
//            return bookmarkRepository.findByUserIdAndCollectionId(userId, collectionId);
//        } else {
//            return bookmarkRepository.findByUserId(userId);
//        }
//    }
}
