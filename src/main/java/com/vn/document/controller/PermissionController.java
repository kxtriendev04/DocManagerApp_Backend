package com.vn.document.controller;

import com.vn.document.domain.Document;
import com.vn.document.domain.Permission;
import com.vn.document.domain.User;
import com.vn.document.domain.dto.request.ShareRequest;
import com.vn.document.domain.dto.request.UpdatePermissionRequest;
import com.vn.document.service.DocumentService;
import com.vn.document.service.PermissionService;
import com.vn.document.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final UserService userService;
    private final DocumentService documentService;

    // API để tạo quyền phân quyền tài liệu
    @PostMapping
    public ResponseEntity<Permission> createPermission(@RequestBody Permission permission) {
        Permission createdPermission = permissionService.createPermission(permission);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPermission);
    }

    //API thêm quyền cho user bằng email
    @PostMapping("/email")
    public ResponseEntity<Permission> shareDocumentByEmail(@RequestBody ShareRequest request) {
        User user = userService.handleFindUserByEmail(request.getEmail());
        Document document = documentService.handleFindDocumentById(request.getDocumentId());

        Permission permission = new Permission();
        permission.setUser(user);
        permission.setDocument(document);
        permission.setPermissionType(request.getPermissionType());

        Permission created = permissionService.createPermission(permission);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // API để lấy quyền của người dùng theo userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Permission>> getPermissionsByUserId(@PathVariable Long userId) {
        List<Permission> permissions = permissionService.getPermissionsByUserId(userId);
        if (permissions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(permissions);
    }

    // API để lấy quyền của tài liệu theo docId
    @GetMapping("/document/{docId}")
    public ResponseEntity<List<Permission>> getPermissionsByDocumentId(@PathVariable Long docId) {
        List<Permission> permissions = permissionService.getPermissionsByDocumentId(docId);
        if (permissions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(permissions);
    }

    // API sửa quyền cho 1 tài liệu
    @PutMapping
    public ResponseEntity<Permission> updatePermissionByUserAndDoc(@RequestBody UpdatePermissionRequest request) {
        try {
            User user = userService.handleFindUserByEmail(request.getEmail());
            Permission updated = permissionService.updatePermissionByUserAndDocument(
                    user.getId(),
                    request.getDocumentId(),
                    request.getPermissionType()
            );
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    // API để xóa quyền
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
