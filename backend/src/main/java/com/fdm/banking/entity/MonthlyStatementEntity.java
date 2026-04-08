package com.fdm.banking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Monthly statement entity. (T078)
 * Table: monthly_statements — append-only, versioned.
 */
@Entity
@Table(name = "monthly_statements",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_ms_account_period_version",
                columnNames = {"account_id", "period", "version_number"}),
        indexes = @Index(name = "idx_ms_account_period", columnList = "account_id, period"))
public class MonthlyStatementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statement_id")
    private Long statementId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 7)
    private String period;

    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "closing_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal closingBalance;

    @Column(name = "total_money_in", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalMoneyIn;

    @Column(name = "total_money_out", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalMoneyOut;

    @Column(name = "transactions_json", nullable = false, columnDefinition = "LONGTEXT")
    private String transactionsJson;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber = 1;

    @Column(name = "correction_summary", length = 2000)
    private String correctionSummary;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }

    public Long getStatementId() { return statementId; }
    public void setStatementId(Long statementId) { this.statementId = statementId; }
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
    public String getTransactionsJson() { return transactionsJson; }
    public void setTransactionsJson(String transactionsJson) { this.transactionsJson = transactionsJson; }
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public String getCorrectionSummary() { return correctionSummary; }
    public void setCorrectionSummary(String correctionSummary) { this.correctionSummary = correctionSummary; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
