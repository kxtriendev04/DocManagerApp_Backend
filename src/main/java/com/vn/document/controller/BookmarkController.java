package com.vn.document.controller;

import com.vn.document.domain.Bookmark;
import com.vn.document.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

//    @GetMapping("/user/{userId}")
//    public ResponseEntity<Map<String, Object>> getBookmarksByUserId(
//            @PathVariable Long userId,
//            @RequestParam(defaultValue = "createdAt") String sortBy,
//            @RequestParam(defaultValue = "desc") String sortDir) {
//        try {
//            List<Bookmark> bookmarks = bookmarkService.getBookmarksByUserId(userId, sortBy, sortDir);
//            Map<String, Object> response = new HashMap<>();
//            response.put("error", null);
//            response.put("message", "Success");
//            response.put("results", bookmarks);
//            response.put("status_code", 200);
//            return ResponseEntity.ok(response);
//        } catch (IllegalArgumentException e) {
//            Map<String, Object> response = new HashMap<>();
//            response.put("error", e.getMessage());
//            response.put("message", "Failed to retrieve bookmarks");
//            response.put("results", null);
//            response.put("status_code", 400);
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
@GetMapping("/{id}")
public ResponseEntity<Map<String, Object>> getBookmarkById(@PathVariable Long id) {
    try {
        Bookmark bookmark = bookmarkService.getBookmarkById(id);
        if (bookmark == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Bookmark not found");
            response.put("message", "Bookmark not found");
            response.put("results", null);
            response.put("status_code", 404);
            return ResponseEntity.status(404).body(response);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("error", null);
        response.put("message", "Success");
        response.put("results", bookmark);
        response.put("status_code", 200);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", e.getMessage());
        response.put("message", "Failed to retrieve bookmark");
        response.put("results", null);
        response.put("status_code", 500);
        return ResponseEntity.status(500).body(response);
    }
}
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getBookmarksByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            List<Bookmark> bookmarks = bookmarkService.getBookmarksByUserId(userId, sortBy, sortDir);
            Map<String, Object> response = new HashMap<>();
            response.put("error", null);
            response.put("message", "Success");
            response.put("results", bookmarks); // Trả về mảng trực tiếp
            response.put("status_code", 200);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve bookmarks");
            response.put("results", List.of());
            response.put("status_code", 400);
            return ResponseEntity.badRequest().body(response);
        }
    }
    @PostMapping
    public ResponseEntity<Bookmark> createBookmark(@RequestBody Bookmark bookmark) {
        Bookmark createdBookmark = bookmarkService.createBookmark(bookmark);
        return ResponseEntity.status(201).body(createdBookmark);
    }

    @PatchMapping("/{id}/toggle-favorite")
    public ResponseEntity<Bookmark> toggleFavorite(@PathVariable Long id) {
        try {
            Bookmark updated = bookmarkService.toggleFavorite(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long id) {
        try {
            bookmarkService.deleteBookmark(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
    }
}