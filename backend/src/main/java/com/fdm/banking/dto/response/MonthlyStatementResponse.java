package com.fdm.banking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Monthly statement response DTO. (T080)
 */
public class MonthlyStatementResponse {
    private Long accountId;
    private String period;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal totalMoneyIn;
    private BigDecimal totalMoneyOut;
    private List<TransactionItemResponse> transactions;
    private Integer versionNumber;
    private String correctionSummary;
    private LocalDateTime generatedAt;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }
    public BigDecimal getClosingBalance() { return closingBalance; }
    public void setClosingBalance(BigDecimal closingBalance) { this.closingBalance = closingBalance; }
    public BigDecimal getTotalMoneyIn() { return totalMoneyIn; }
    public void setTotalMoneyIn(BigDecimal totalMoneyIn) { this.totalMoneyIn = totalMoneyIn; }
    public BigDecimal getTotalMoneyOut() { return totalMoneyOut; }
    public void setTotalMoneyOut(BigDecimal totalMoneyOut) { this.totalMoneyOut = totalMoneyOut; }
    public List<TransactionItemResponse> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionItemResponse> transactions) { this.transactions = transactions; }
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public String getCorrectionSummary() { return correctionSummary; }
    public void setCorrectionSummary(String correctionSummary) { this.correctionSummary = correctionSummary; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
