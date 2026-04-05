package com.pharma.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true)
    public UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tender_id")
    public Tender tender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    public User uploadedBy;

    @NotBlank
    @Size(max = 255)
    @Column(name = "file_name", nullable = false)
    public String fileName;

    @NotBlank
    @Size(max = 500)
    @Column(name = "file_path", nullable = false)
    public String filePath;

    @NotNull
    @Column(name = "file_size", nullable = false)
    public Long fileSize;

    @NotBlank
    @Size(max = 100)
    @Column(name = "mime_type", nullable = false)
    public String mimeType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    public DocumentType documentType;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(nullable = false)
    public Boolean verified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    public User verifiedBy;

    @Column(name = "verified_at")
    public LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
    }

    public enum DocumentType {
        CERTIFICATE, LICENSE, APPROVAL, QUALITY_REPORT, OTHER
    }

    public static Document findByUuid(UUID uuid) {
        return find("uuid", uuid).firstResult();
    }
}
