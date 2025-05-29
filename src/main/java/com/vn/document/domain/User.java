package com.vn.document.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vn.document.util.RoleEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

// lombok
@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String fullName;

    @Email
    @NotNull
    @Column(unique = true)
    private String email;

    @NotNull
    @Size(min = 6, message = "Mật khẩu phải chứa 6 ký tự trở lên")
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private RoleEnum role; // USER | ADMIN

    private String status;
    private String avatarUrl;
    private Instant createdAt;

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;


    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
    }
}
