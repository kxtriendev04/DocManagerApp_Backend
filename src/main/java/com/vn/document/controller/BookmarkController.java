package com.vn.document.controller;

import com.vn.document.domain.Bookmark;
import com.vn.document.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // Lấy tất cả các bookmark của người dùng
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Bookmark>> getBookmarksByUserId(@PathVariable Long userId) {
        List<Bookmark> bookmarks = bookmarkService.getBookmarksByUserId(userId);
        if (bookmarks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(bookmarks);
    }

    // Lấy tất cả các bookmark của tài liệu
    @GetMapping("/document/{docId}")
    public ResponseEntity<List<Bookmark>> getBookmarksByDocId(@PathVariable Long docId) {
        List<Bookmark> bookmarks = bookmarkService.getBookmarksByDocId(docId);
        if (bookmarks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(bookmarks);
    }

    // Tạo một bookmark mới
    @PostMapping
    public ResponseEntity<Bookmark> createBookmark(@RequestBody Bookmark bookmark) {
        Bookmark createdBookmark = bookmarkService.createBookmark(bookmark);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBookmark);
    }

    // Xóa một bookmark
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long id) {
        bookmarkService.deleteBookmark(id);
        return ResponseEntity.noContent().build();
    }
    // Tìm kiếm theo tiêu đề
    @GetMapping("/search")
    public ResponseEntity<List<Bookmark>> searchBookmarks(
            @RequestParam Long userId,
            @RequestParam String keyword
    ) {
        List<Bookmark> bookmarks = bookmarkService.searchBookmarks(userId, keyword);
        return ResponseEntity.ok(bookmarks);
    }

    // Đánh dấu yêu thích
    @PatchMapping("/{id}/toggle-favorite")
    public ResponseEntity<Bookmark> toggleFavorite(@PathVariable Long id) {
        Bookmark updated = bookmarkService.toggleFavorite(id);
        return ResponseEntity.ok(updated);
    }

    // Cập nhật thông tin bookmark
    @PutMapping("/{id}")
    public ResponseEntity<Bookmark> updateBookmark(@PathVariable Long id, @RequestBody Bookmark updatedBookmark) {
        Bookmark result = bookmarkService.updateBookmark(id, updatedBookmark);
        return ResponseEntity.ok(result);
    }

    //Xem chi tiêt bookmark
    @GetMapping("/{id}")
    public ResponseEntity<Bookmark> getBookmarkById(@PathVariable Long id) {
        Bookmark bookmark = bookmarkService.getBookmarkById(id);
        if (bookmark == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(bookmark);
    }

    //Di chuyển đến 1 collection khác
    @PatchMapping("/{id}/move-to-collection")
    public ResponseEntity<Bookmark> moveToCollection(
            @PathVariable Long id,
            @RequestParam Long newCollectionId
    ) {
        Bookmark updated = bookmarkService.moveToCollection(id, newCollectionId);
        return ResponseEntity.ok(updated);
    }

    //Lọc theo category hoặc collection
//    @GetMapping("/filter")
//    public ResponseEntity<List<Bookmark>> filterBookmarks(
//            @RequestParam Long userId,
//            @RequestParam(required = false) Long categoryId,
//            @RequestParam(required = false) Long collectionId
//    ) {
//        List<Bookmark> bookmarks = bookmarkService.filterBookmarks(userId, categoryId, collectionId);
//        return ResponseEntity.ok(bookmarks);
//    }
}
