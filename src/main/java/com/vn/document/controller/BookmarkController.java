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

    //Tạo một bookmark mới
    @PostMapping
    public ResponseEntity<Bookmark> createBookmark(@RequestBody Bookmark bookmark) {
        Bookmark createdBookmark = bookmarkService.createBookmark(bookmark);
        return ResponseEntity.status(201).body(createdBookmark);
    }

    //Lấy tất cả các bookmark của người dùng
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Bookmark>> getBookmarksByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            List<Bookmark> bookmarks = bookmarkService.getBookmarksByUserId(userId, sortBy, sortDir);
            return ResponseEntity.ok(bookmarks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(List.of());
        }
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

    //Xóa một bookmark
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long id) {
        try {
            bookmarkService.deleteBookmark(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
    }

    // Cập nhật thông tin bookmark
    @PutMapping("/{id}")
    public ResponseEntity<Bookmark> updateBookmark(@PathVariable Long id, @RequestBody Bookmark updatedBookmark) {
        Bookmark result = bookmarkService.updateBookmark(id, updatedBookmark);
        return ResponseEntity.ok(result);
    }

    //Xem chi tiết bookmark
    @GetMapping("/{id}")
    public ResponseEntity<Bookmark> getBookmarkById(@PathVariable Long id) {
        try {
            Bookmark bookmark = bookmarkService.getBookmarkById(id);
            if (bookmark == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(bookmark);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}