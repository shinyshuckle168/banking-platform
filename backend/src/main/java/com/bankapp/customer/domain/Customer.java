package com.bankapp.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(nullable = false)
    private UUID ownerUserId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType type;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Customer() {
    }

    public Customer(UUID ownerUserId, String name, String address, CustomerType type) {
        Instant now = normalizeTimestamp(Instant.now());
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.address = address;
        this.type = type;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PrePersist
    void prePersist() {
        Instant now = normalizeTimestamp(Instant.now());
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = normalizeTimestamp(Instant.now());
    }

    private static Instant normalizeTimestamp(Instant value) {
        return value.truncatedTo(ChronoUnit.MILLIS);
    }

    public Long getCustomerId() {
        return customerId;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public CustomerType getType() {
        return type;
    }

    public void setType(CustomerType type) {
        this.type = type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
