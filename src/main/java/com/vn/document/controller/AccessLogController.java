package com.vn.document.controller;

import com.vn.document.domain.AccessLog;
import com.vn.document.service.AccessLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/access-logs")
@RequiredArgsConstructor
public class AccessLogController {

    private final AccessLogService accessLogService;

    // API để lấy tất cả nhật ký truy cập của người dùng theo userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccessLog>> getLogsByUserId(@PathVariable Long userId) {
        List<AccessLog> logs = accessLogService.getLogsByUserId(userId);
        if (logs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(logs);
    }

    // API để lấy tất cả nhật ký truy cập của tài liệu theo docId
    @GetMapping("/document/{docId}")
    public ResponseEntity<List<AccessLog>> getLogsByDocumentId(@PathVariable Long docId) {
        List<AccessLog> logs = accessLogService.getLogsByDocumentId(docId);
        if (logs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(logs);
    }

    // API để tạo nhật ký truy cập mới
    @PostMapping
    public ResponseEntity<AccessLog> createAccessLog(@RequestBody AccessLog accessLog) {
        AccessLog createdLog = accessLogService.createAccessLog(accessLog);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLog);
    }
}
