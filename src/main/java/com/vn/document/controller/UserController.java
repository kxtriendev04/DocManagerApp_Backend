package com.vn.document.controller;

import com.vn.document.domain.User;
import com.vn.document.service.UserService;

import com.vn.document.util.RoleEnum;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/users") // Bắt đầu bằng api/v1/users
public class UserController {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public UserController(PasswordEncoder passwordEncoder, UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    // Tạo user
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        if (user.getPassword() != null) {
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);
        }
        user.setRole(RoleEnum.USER);
        User newUser = userService.handlecreateUser(user);
        return ResponseEntity.ok(newUser);
    }

    // Lấy tất cả users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> list = userService.handleGetAllUsers();
        return ResponseEntity.ok(list);
    }

    // Tìm user theo ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") long id) {
        User user = userService.handleGetUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    // Xóa user theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteUser(@PathVariable("id") long id) {
        long deletedId = userService.handleDeleteUser(id);
        return ResponseEntity.ok(deletedId);
    }

    // update user
    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody User user) throws NotFoundException {
        User updatedUser = userService.handleGetUserById(user.getId());
        updatedUser.setFullName(user.getFullName());
        updatedUser.setEmail(user.getEmail());
        if (updatedUser != null) {
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                updatedUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            userService.handleUpdateUser(user);
        } else {
            throw new NotFoundException();
        }
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/myinfor/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.handleGetUserByUsername(email);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

//@GetMapping("/myinfor")
//public ResponseEntity<User> getMyInfor(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
//    String email = principal.getUsername(); // hoặc getEmail() nếu custom UserDetails
//
//    User user = userService.handleGetUserByUsername(email);
//    return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
//}

}
