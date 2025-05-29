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
}
