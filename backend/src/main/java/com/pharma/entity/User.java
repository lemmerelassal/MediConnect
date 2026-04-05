package com.pharma.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@UserDefinition
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    @Username
    public String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false)
    @Password
    public String passwordHash;

    @Size(max = 100)
    @Column(name = "first_name")
    public String firstName;

    @Size(max = 100)
    @Column(name = "last_name")
    public String lastName;

    @NotBlank
    @Column(nullable = false)
    @Roles
    public String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    public Country country;

    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;

    @Column(name = "email_verified", nullable = false)
    public Boolean emailVerified = false;

    @Column(name = "last_login")
    public LocalDateTime lastLogin;

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

    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public enum Role {
        ADMIN, COUNTRY_ADMIN, SUPPLIER, VIEWER
    }
}
