package com.vn.document.controller;

import com.vn.document.domain.Permission;
import com.vn.document.service.PermissionService;
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

    // API để tạo quyền phân quyền tài liệu
    @PostMapping
    public ResponseEntity<Permission> createPermission(@RequestBody Permission permission) {
        Permission createdPermission = permissionService.createPermission(permission);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPermission);
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

    // API để xóa quyền
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
