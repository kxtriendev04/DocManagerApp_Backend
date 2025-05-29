package com.vn.document.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "password")
    private String password;

    @Column(name = "encryption_method")
    private String encryptionMethod;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

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
