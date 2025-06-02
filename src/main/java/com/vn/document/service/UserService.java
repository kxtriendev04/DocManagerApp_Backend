package com.vn.document.service;

import com.vn.document.domain.User;
import com.vn.document.repository.UserRepository;
import com.vn.document.util.RoleEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleGetUserByUsername(String email) {
        Optional<User> oUser = userRepository.findByEmail(email);
        if (oUser.isPresent())
            return oUser.get();
        else
            throw new RuntimeException("Không tìm thấy User");

    }

    public User handlecreateUser(User user) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        user.setRole(RoleEnum.valueOf("USER"));
        user.setAvatarUrl("/storage/");
        return userRepository.save(user);
    }

    public List<User> handleGetAllUsers() {
        return userRepository.findAll();
    }

    // Tìm user theo ID
    public User handleGetUserById(long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Xóa user theo ID
    public long handleDeleteUser(long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return id;
        }
        return 0;
    }

    public User handleUpdateUser(User user) {
        User userUpdate = userRepository.findByEmail(user.getEmail()).orElseThrow();
        if (user.getFullName() != null) userUpdate.setFullName(user.getFullName());
        if (user.getAvatarUrl() != null) userUpdate.setAvatarUrl(user.getAvatarUrl());
        if (user.getRole() != null) userUpdate.setRole(user.getRole());

        // Không cập nhật email và password nếu không cần thiết
        // Nếu có logic cập nhật mật khẩu thì nên mã hóa lại trước khi lưu

        return userRepository.save(userUpdate);
    }

    public void handleUpdateRefreshToken(String token, String email) {
        User user = this.handleGetUserByUsername(email);
        user.setRefreshToken(token);
        this.userRepository.save(user);
    }
    public User handleFindUserById(Long id){
        return userRepository.findById(id).orElseThrow(()->new RuntimeException("User không tồn tại!"));
    }

    public User handleFindUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }


    public User getUserByRefreshTokenAndEmail(String token, String email) {
        User user = userRepository.findByRefreshTokenAndEmail(token, email);
        if (user != null)
            return user;
        else
            throw new RuntimeException("Refresh token không hợp lệ!!!");
    }
}
