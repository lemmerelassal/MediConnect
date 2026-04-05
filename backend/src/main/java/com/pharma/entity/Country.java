package com.pharma.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "countries")
public class Country extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true)
    public String name;

    @NotBlank
    @Size(max = 3)
    @Column(name = "country_code", nullable = false, unique = true)
    public String countryCode;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(name = "contact_email", nullable = false)
    public String contactEmail;

    @Size(max = 50)
    @Column(name = "contact_phone")
    public String contactPhone;

    @Size(max = 50)
    @Column(nullable = false)
    public String timezone = "UTC";

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
