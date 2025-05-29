package com.vn.document.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "access_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "doc_id", nullable = false)
    private Document document;

    @Column(name = "access_time", nullable = false)
    private Timestamp accessTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ActionType action;

    public enum ActionType {
        VIEW, EDIT, DOWNLOAD
    }

    @PrePersist
    public void handleBeforeCreate() {
        this.accessTime = new Timestamp(System.currentTimeMillis());
    }
}
