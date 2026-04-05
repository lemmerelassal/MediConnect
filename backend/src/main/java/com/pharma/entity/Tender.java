package com.pharma.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenders")
public class Tender extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shortage_id", nullable = false)
    public Shortage shortage;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_country_id", nullable = false)
    public Country supplierCountry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    public User submittedBy;

    @NotNull
    @Column(name = "quantity_offered", nullable = false)
    public Integer quantityOffered;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    public String unit;

    @NotNull
    @Column(name = "price_per_unit", nullable = false, precision = 10, scale = 2)
    public BigDecimal pricePerUnit;

    @NotBlank
    @Size(max = 3)
    @Column(nullable = false)
    public String currency = "USD";

    @NotNull
    @Column(name = "delivery_time_days", nullable = false)
    public Integer deliveryTimeDays;

    @Size(max = 255)
    @Column(name = "manufacturer_name")
    public String manufacturerName;

    @Size(max = 100)
    @Column(name = "batch_number")
    public String batchNumber;

    @Column(name = "expiry_date")
    public LocalDate expiryDate;

    @Column(name = "regulatory_approval_info", columnDefinition = "TEXT")
    public String regulatoryApprovalInfo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TenderStatus status = TenderStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    public String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    public User reviewedBy;

    @Column(name = "reviewed_at")
    public LocalDateTime reviewedAt;

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

    public enum TenderStatus {
        PENDING, ACCEPTED, REJECTED, WITHDRAWN
    }
}
