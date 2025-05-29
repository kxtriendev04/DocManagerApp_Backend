package com.vn.document.service;

import com.vn.document.domain.AccessLog;
import com.vn.document.domain.Document;
import com.vn.document.domain.User;
import com.vn.document.repository.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessLogService {
    private final UserService userService;
    private final DocumentService documentService;
    private final AccessLogRepository accessLogRepository;

    // Lấy tất cả nhật ký truy cập của người dùng theo userId
    public List<AccessLog> getLogsByUserId(Long userId) {
        return accessLogRepository.findByUserId(userId);
    }

    // Lấy tất cả nhật ký truy cập của tài liệu theo docId
    public List<AccessLog> getLogsByDocumentId(Long docId) {
        return accessLogRepository.findByDocumentId(docId);
    }

    // Tạo nhật ký truy cập
    public AccessLog createAccessLog(AccessLog accessLog) {
        User user = userService.handleFindUserById(accessLog.getUser().getId());
        Document document = documentService.handleFindDocumentById(accessLog.getDocument().getId());
        accessLog.setUser(user);
        accessLog.setDocument(document);
        return accessLogRepository.save(accessLog);
    }
}
