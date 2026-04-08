package com.fdm.banking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Single transaction item in history/statement responses. (T024)
 */
public class TransactionItemResponse {
    private Long transactionId;
    private BigDecimal amount;
    private String type;
    private String status;
    private LocalDateTime timestamp;
    private String description;
    private String idempotencyKey;
    private String category;

    public TransactionItemResponse() {}

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
