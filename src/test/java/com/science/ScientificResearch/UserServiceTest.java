package com.science.ScientificResearch;

import com.vn.document.domain.User;
import com.vn.document.repository.UserRepository;
import com.vn.document.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class); // Tạo mock repository
        userService = new UserService(userRepository); // Inject mock vào service
    }

    @Test
    void testCreateUser_Success() {
        // Arrange
        User user = new User();
        user.setEmail("newuser@example.com");

        // Giả sử chưa có user nào dùng email này
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        // Giả lập lưu user thành công
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User createdUser = userService.handlecreateUser(user);

        // Assert
        assertEquals(user.getEmail(), createdUser.getEmail());
        verify(userRepository, times(1)).save(user); // Đảm bảo save được gọi 1 lần
    }

    @Test
    void testCreateUser_EmailAlreadyExists() {
        // Arrange
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");

        when(userRepository.findByEmail(existingUser.getEmail())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.handlecreateUser(existingUser);
        });

        assertEquals("Email đã được sử dụng!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Không được gọi save
    }
}
