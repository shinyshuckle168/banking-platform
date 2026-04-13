package com.fdm.banking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Group 2 entity — imported from Group 2 module.
 * Import path: com.fdm.banking.entity.UserEntity
 * See SecurityConfig.java for Group 2 import documentation.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Column(nullable = false)
    private boolean enabled = true;

    /** Stores Group 1's UUID so the filter can resolve the JWT sub claim. */
    @Column(name = "external_subject_id", length = 50)
    private String externalSubjectId;

    /** FK to Group 1's Customer record — used to populate UserPrincipal.customerId. */
    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public RoleEntity getRole() { return role; }
    public void setRole(RoleEntity role) { this.role = role; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getExternalSubjectId() { return externalSubjectId; }
    public void setExternalSubjectId(String externalSubjectId) { this.externalSubjectId = externalSubjectId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
