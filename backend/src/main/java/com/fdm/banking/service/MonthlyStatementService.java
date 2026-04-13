package com.fdm.banking.service;

import com.fdm.banking.entity.*;
import com.fdm.banking.exception.*;
import com.fdm.banking.repository.AccountRepository;
import com.fdm.banking.repository.ExportCacheRepository;
import com.fdm.banking.repository.TransactionQueryRepository;
import com.fdm.banking.security.OwnershipValidator;
import com.fdm.banking.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HexFormat;
import java.util.List;

/**
 * Monthly statement service — generates PDF on demand from live transaction data. (T082)
 */
@Service
public class MonthlyStatementService {

    private static final DateTimeFormatter PERIOD_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final AccountRepository accountRepository;
    private final TransactionQueryRepository transactionQueryRepository;
    private final ExportCacheRepository exportCacheRepository;
    private final OwnershipValidator ownershipValidator;
    private final PdfStatementService pdfStatementService;
    private final AuditService auditService;

    public MonthlyStatementService(AccountRepository accountRepository,
                                    TransactionQueryRepository transactionQueryRepository,
                                    ExportCacheRepository exportCacheRepository,
                                    OwnershipValidator ownershipValidator,
                                    PdfStatementService pdfStatementService,
                                    AuditService auditService) {
        this.accountRepository = accountRepository;
        this.transactionQueryRepository = transactionQueryRepository;
        this.exportCacheRepository = exportCacheRepository;
        this.ownershipValidator = ownershipValidator;
        this.pdfStatementService = pdfStatementService;
        this.auditService = auditService;
    }

    /**
     * Generates a monthly statement PDF on demand. (T082)
     */
    public byte[] generateStatement(long accountId, String period, UserPrincipal caller) {
        // Re-validate permission at delivery
        if (!caller.hasPermission("STATEMENT:READ")) {
            throw new PermissionDeniedException("STATEMENT:READ");
        }
        ownershipValidator.assertOwnership(accountId, caller);

        // Validate period format
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(period, PERIOD_FMT);
        } catch (DateTimeParseException e) {
            throw new SemanticValidationException("Invalid period format. Expected YYYY-MM",
                    "ERR_INVALID_PERIOD_FORMAT", "period");
        }

        // Reject future months (month not yet started)
        YearMonth currentMonth = YearMonth.now();
        if (yearMonth.isAfter(currentMonth)) {
            throw new BusinessStateException("Future month requested",
                    "ERR_FUTURE_MONTH", "period");
        }

        // Load account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found", "ERR_ACC_NOT_FOUND"));

        // Check cache first (idempotency)
        String cacheKey = computeHash(accountId, period);
        return exportCacheRepository.findByAccountIdAndParamHash(accountId, cacheKey)
                .map(ExportCacheEntity::getPdfData)
                .orElseGet(() -> {
                    byte[] pdf = buildAndCachePdf(account, yearMonth, currentMonth, cacheKey);
                    auditService.log(caller.getUserId(), caller.getRole(),
                            "STATEMENT_GENERATED", "STATEMENT", accountId + "/" + period, "SUCCESS");
                    return pdf;
                });
    }

    private byte[] buildAndCachePdf(Account account, YearMonth yearMonth,
                                     YearMonth currentMonth, String cacheKey) {
        long accountId = account.getAccountId();
        LocalDateTime periodStart = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime periodEnd = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> transactions =
                transactionQueryRepository.findByAccount_AccountIdAndTimestampBetweenOrderByTimestampAsc(
                        accountId, periodStart, periodEnd);

        // Calculate balances
        BigDecimal totalIn = BigDecimal.ZERO;
        BigDecimal totalOut = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.getStatus() == TransactionStatus.SUCCESS) {
                if (t.getDirection() == TransactionDirection.CREDIT) {
                    totalIn = totalIn.add(t.getAmount());
                } else if (t.getDirection() == TransactionDirection.DEBIT || t.getDirection() == TransactionDirection.TRANSFER) {
                    totalOut = totalOut.add(t.getAmount());
                }
            }
        }

        // Opening balance = current balance - net activity in period
        BigDecimal closingBalance = account.getBalance();
        BigDecimal openingBalance = closingBalance.subtract(totalIn).add(totalOut);

        boolean isMonthToDate = yearMonth.equals(currentMonth);

        Customer customer = account.getCustomer();
        String customerName = customer != null ? customer.getName() : null;

        byte[] pdfBytes = pdfStatementService.buildStatementPdf(
                accountId,
                account.getAccountNumber(),
                account.getStatus().name(),
                customerName,
                yearMonth,
                isMonthToDate,
                openingBalance,
                closingBalance,
                totalIn,
                totalOut,
                transactions);

        ExportCacheEntity cache = new ExportCacheEntity();
        cache.setAccountId(accountId);
        cache.setParamHash(cacheKey);
        cache.setPdfData(pdfBytes);
        exportCacheRepository.save(cache);
        return pdfBytes;
    }

    /**
     * For TRANSFER transactions, we treat all TRANSFER amounts as debits (money out).
     * The direction is determined by the transaction description or type context.
     * Since TransactionEntity does not distinguish TRANSFER_IN from TRANSFER_OUT,
     * all TRANSFER transactions with SUCCESS status are treated as money out.
     */
    private boolean isCredit(Transaction t, long accountId) {
        // TRANSFER transactions in this model represent outgoing transfers
        return false;
    }

    private String computeHash(long accountId, String period) {
        String input = "STATEMENT|" + accountId + "|" + period;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
