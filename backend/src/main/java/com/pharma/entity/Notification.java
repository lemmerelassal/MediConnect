package com.pharma.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    public String type;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    public String title;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    public String message;

    @Size(max = 50)
    @Column(name = "related_entity_type")
    public String relatedEntityType;

    @Column(name = "related_entity_id")
    public Long relatedEntityId;

    @Column(name = "is_read", nullable = false)
    public Boolean isRead = false;

    @Column(name = "read_at")
    public LocalDateTime readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public static long countUnread(Long userId) {
        return count("user.id = ?1 and isRead = false", userId);
    }

    public enum NotificationType {
        NEW_SHORTAGE, NEW_TENDER, TENDER_ACCEPTED, TENDER_REJECTED, 
        SHORTAGE_FULFILLED, DEADLINE_APPROACHING, DOCUMENT_UPLOADED, DOCUMENT_VERIFIED
    }
}
