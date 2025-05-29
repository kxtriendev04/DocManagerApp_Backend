package com.vn.document.config;

import com.vn.document.domain.User;
import com.vn.document.repository.UserRepository;
import com.vn.document.util.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class ApplicationInitConfig {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {

        return args -> {
//            Kiểm tra sự tồn tại của admin
            Optional<User> adminUser = userRepository.findByEmail("admin@admin.com");
            if (adminUser.isEmpty()) {
                User admin = new User();
                admin.setFullName("Quản trị viên");
                admin.setEmail("admin@admin.com");
                admin.setPassword(passwordEncoder.encode("123456")); // Mã hóa mật khẩu
                admin.setRole(RoleEnum.ADMIN);

                userRepository.save(admin); // Lưu vào database
                System.out.println("Create default admin successfully!");
            }
        };
    }
}
