package com.vn.document.service;

import com.vn.document.domain.Document;
import com.vn.document.domain.Permission;
import com.vn.document.domain.User;
import com.vn.document.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final UserService userService;
    private final DocumentService documentService;
    private final PermissionRepository permissionRepository;

    // Tạo quyền mới
    public Permission createPermission(Permission permission) {
        User user = userService.handleFindUserById(permission.getUser().getId());
        Document document = documentService.handleFindDocumentById(permission.getUser().getId());
        permission.setUser(user);
        permission.setDocument(document);
        return permissionRepository.save(permission);
    }

    // Lấy tất cả quyền của người dùng theo userId
    public List<Permission> getPermissionsByUserId(Long userId) {
        return permissionRepository.findByUserId(userId);
    }

    // Lấy tất cả quyền của tài liệu theo docId
    public List<Permission> getPermissionsByDocumentId(Long docId) {
        return permissionRepository.findByDocumentId(docId);
    }

    // Xóa quyền
    public void deletePermission(Long permissionId) {
        permissionRepository.deleteById(permissionId);
    }
}
