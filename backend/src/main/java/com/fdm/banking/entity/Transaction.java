package com.fdm.banking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Group 2 entity — extended by Group 3 (GAP-1: PENDING status added; GAP for category column).
 * Import path: com.fdm.banking.entity.TransactionEntity
 * Group 3 coordination notes:
 *   - PENDING added to TransactionStatus enum (T119)
 *   - category column added nullable VARCHAR(50) (T092)
 */
@Entity
@Table(name = "bank_transaction")
public class Transaction {

    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(length = 255)
    private String description;

    @Column(name = "sender_info", length = 100)
    private String senderInfo;

    @Column(name = "receiver_info", length = 100)
    private String receiverInfo;

    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @Column(length = 50)
    private String category;

    @Column(name = "external_transaction_id", length = 36, unique = true)
    private String externalTransactionId;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (externalTransactionId == null) {
            externalTransactionId = UUID.randomUUID().toString();
        }
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public TransactionDirection getDirection() { return direction; }
    public void setDirection(TransactionDirection direction) { this.direction = direction; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSenderInfo() { return senderInfo; }
    public void setSenderInfo(String senderInfo) { this.senderInfo = senderInfo; }
    public String getReceiverInfo() { return receiverInfo; }
    public void setReceiverInfo(String receiverInfo) { this.receiverInfo = receiverInfo; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getExternalTransactionId() { return externalTransactionId; }
    public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }
}
