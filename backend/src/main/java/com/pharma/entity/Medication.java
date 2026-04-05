package com.pharma.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "medications")
public class Medication extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "generic_name", nullable = false)
    public String genericName;

    @Size(max = 255)
    @Column(name = "brand_name")
    public String brandName;

    @Size(max = 100)
    @Column(name = "dosage_form")
    public String dosageForm;

    @Size(max = 100)
    public String strength;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Size(max = 20)
    @Column(name = "atc_code")
    public String atcCode;

    @Size(max = 100)
    @Column(name = "therapeutic_category")
    public String therapeuticCategory;

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
}
