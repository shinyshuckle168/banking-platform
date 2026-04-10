package com.fdm.banking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Group 2 entity — extended by Group 3 (GAP-2: daily_transfer_limit column added).
 * Import path: com.fdm.banking.entity.AccountEntity
 * Group 3 coordination note: daily_transfer_limit column added per data-model.md GAP-2.
 * Flyway migration required for production — column added here for H2 DDL auto-create.
 */
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyTransferLimit = new BigDecimal("3000.00");

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (dailyTransferLimit == null) {
            dailyTransferLimit = new BigDecimal("3000.00");
        }
    }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public CustomerEntity getCustomer() { return customer; }
    public void setCustomer(CustomerEntity customer) { this.customer = customer; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public BigDecimal getDailyTransferLimit() { return dailyTransferLimit; }
    public void setDailyTransferLimit(BigDecimal dailyTransferLimit) { this.dailyTransferLimit = dailyTransferLimit; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}
