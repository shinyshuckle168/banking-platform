package com.fdm.banking.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transaction history response. (T024)
 */
public class TransactionHistoryResponse {
    private Long accountId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int transactionCount;
    private List<TransactionItemResponse> transactions;

    public TransactionHistoryResponse() {}

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    public List<TransactionItemResponse> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionItemResponse> transactions) { this.transactions = transactions; }
}
