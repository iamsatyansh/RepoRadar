package com.reporadar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class UserAccount extends BaseEntity {
    @Column(nullable = false, unique = true, length = 64)
    private String username;
    @Column(nullable = false, unique = true, length = 320)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DomainEnums.UserRole role = DomainEnums.UserRole.USER;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    @OneToMany(mappedBy = "requestedBy")
    private List<Analysis> analyses = new ArrayList<>();

    protected UserAccount() { }
    public UserAccount(String username, String email, String passwordHash) { this.username = username; this.email = email; this.passwordHash = passwordHash; }
    public void touch() { updatedAt = Instant.now(); }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public DomainEnums.UserRole getRole() { return role; }
}
