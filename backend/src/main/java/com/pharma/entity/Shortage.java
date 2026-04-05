package com.pharma.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shortages")
public class Shortage extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id", nullable = false)
    public Country country;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medication_id", nullable = false)
    public Medication medication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    public User createdBy;

    @NotNull
    @Column(name = "quantity_needed", nullable = false)
    public Integer quantityNeeded;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    public String unit;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level", nullable = false)
    public UrgencyLevel urgencyLevel;

    @Column(columnDefinition = "TEXT")
    public String reason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ShortageStatus status = ShortageStatus.ACTIVE;

    @Column
    public LocalDateTime deadline;

    @Column(name = "estimated_value", precision = 15, scale = 2)
    public BigDecimal estimatedValue;

    @Size(max = 3)
    @Column(nullable = false)
    public String currency = "USD";

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum UrgencyLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum ShortageStatus {
        ACTIVE, FULFILLED, CANCELLED, EXPIRED
    }
}
