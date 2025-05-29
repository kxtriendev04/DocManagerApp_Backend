package com.vn.document.domain;

import com.vn.document.util.PermissionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "doc_id", nullable = false)
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false)
    private PermissionType permissionType;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}

