package com.vn.document.repository;

import com.vn.document.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    User findByRefreshTokenAndEmail(String refreshToken, String email);

    Optional<User> findById(Long id);

}
