package com.vn.document.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "document_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doc_id", nullable = false)
    private Document document;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "s3_url", nullable = false)
    private String s3Url;

    @Column(name = "file_size", nullable = false)
    private Integer fileSize; // byte

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
