package com.fdm.banking.service;

import com.fdm.banking.dto.response.TransactionHistoryResponse;
import com.fdm.banking.dto.response.TransactionItemResponse;
import com.fdm.banking.entity.AccountEntity;
import com.fdm.banking.entity.AccountStatus;
import com.fdm.banking.entity.ExportCacheEntity;
import com.fdm.banking.entity.TransactionEntity;
import com.fdm.banking.exception.BusinessStateException;
import com.fdm.banking.exception.PermissionDeniedException;
import com.fdm.banking.exception.ResourceNotFoundException;
import com.fdm.banking.exception.RetentionWindowException;
import com.fdm.banking.repository.AccountRepository;
import com.fdm.banking.repository.ExportCacheRepository;
import com.fdm.banking.repository.TransactionQueryRepository;
import com.fdm.banking.security.OwnershipValidator;
import com.fdm.banking.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transaction history service. (T026, T028)
 */
@Service
public class TransactionHistoryService {

    private static final int MAX_RANGE_DAYS = 366;
    private static final int CLOSED_ACCOUNT_WINDOW_DAYS = 90;

    private final TransactionQueryRepository transactionQueryRepository;
    private final AccountRepository accountRepository;
    private final OwnershipValidator ownershipValidator;
    private final PdfStatementService pdfStatementService;
    private final ExportCacheRepository exportCacheRepository;
    private final AuditService auditService;

    public TransactionHistoryService(TransactionQueryRepository transactionQueryRepository,
                                     AccountRepository accountRepository,
                                     OwnershipValidator ownershipValidator,
                                     PdfStatementService pdfStatementService,
                                     ExportCacheRepository exportCacheRepository,
                                     AuditService auditService) {
        this.transactionQueryRepository = transactionQueryRepository;
        this.accountRepository = accountRepository;
        this.ownershipValidator = ownershipValidator;
        this.pdfStatementService = pdfStatementService;
        this.exportCacheRepository = exportCacheRepository;
        this.auditService = auditService;
    }

    /**
     * Retrieves transaction history for an account within a date range. (T026)
     */
    public TransactionHistoryResponse getHistory(long accountId, LocalDate startDate,
                                                  LocalDate endDate, UserPrincipal caller) {
        if (!caller.hasPermission("TRANSACTION:READ")) {
            throw new PermissionDeniedException("TRANSACTION:READ");
        }
        ownershipValidator.assertOwnership(accountId, caller);

        LocalDateTime now = LocalDateTime.now();

        // Reject future startDate
        if (startDate != null && startDate.isAfter(LocalDate.now())) {
            throw new BusinessStateException(
                    "startDate must not be in the future", "ERR_FUTURE_START_DATE", "startDate");
        }

        // Reject endDate before startDate
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessStateException(
                    "endDate must be after startDate", "ERR_END_DATE_BEFORE_START", "endDate");
        }

        // Apply defaults
        LocalDateTime effectiveStart = (startDate != null)
                ? startDate.atStartOfDay()
                : now.minusDays(28);
        LocalDateTime effectiveEnd = (endDate != null)
                ? endDate.atTime(23, 59, 59)
                : now;

        // Override future endDate silently
        if (effectiveEnd.isAfter(now)) {
            effectiveEnd = now;
        }

        // Validate range <= 366 days
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                effectiveStart.toLocalDate(), effectiveEnd.toLocalDate());
        if (daysBetween > MAX_RANGE_DAYS) {
            throw new BusinessStateException(
                    "Date range must not exceed 366 days", "ERR_DATE_RANGE_EXCEEDED", "endDate");
        }

        // Check account status
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new com.fdm.banking.exception.ResourceNotFoundException(
                        "Account not found", "ERR_ACC_NOT_FOUND"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            LocalDateTime closedAt = account.getClosedAt();
            if (closedAt != null && closedAt.plusDays(CLOSED_ACCOUNT_WINDOW_DAYS).isBefore(now)) {
                auditService.log(caller.getUserId(), caller.getRole(),
                        "TRANSACTION_HISTORY_FAILED", "ACCOUNT", String.valueOf(accountId), "DENIED");
                throw new RetentionWindowException(
                        "Account closed and 90-day window has expired", "ERR_RETENTION_WINDOW");
            }
        }

        List<TransactionEntity> transactions =
                transactionQueryRepository.findByAccount_AccountIdAndTimestampBetweenOrderByTimestampAsc(
                        accountId, effectiveStart, effectiveEnd);

        List<TransactionItemResponse> items = transactions.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        auditService.log(caller.getUserId(), caller.getRole(),
                "TRANSACTION_HISTORY", "ACCOUNT", String.valueOf(accountId), "SUCCESS");

        TransactionHistoryResponse response = new TransactionHistoryResponse();
        response.setAccountId(accountId);
        response.setStartDate(effectiveStart);
        response.setEndDate(effectiveEnd);
        response.setTransactionCount(items.size());
        response.setTransactions(items);
        return response;
    }

    /**
     * Exports transaction history as a PDF with idempotent caching. (T028)
     */
    public byte[] exportPdf(long accountId, LocalDate startDate,
                             LocalDate endDate, UserPrincipal caller) {
        if (!caller.hasPermission("TRANSACTION:READ")) {
            throw new PermissionDeniedException("TRANSACTION:READ");
        }
        ownershipValidator.assertOwnership(accountId, caller);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime effectiveStart = (startDate != null) ? startDate.atStartOfDay() : now.minusDays(28);
        LocalDateTime rawEffectiveEnd = (endDate != null) ? endDate.atTime(23, 59, 59) : now;
        LocalDateTime effectiveEnd = rawEffectiveEnd.isAfter(now) ? now : rawEffectiveEnd;

        String paramHash = computeHash(accountId, effectiveStart, effectiveEnd);

        // Check cache
        return exportCacheRepository.findByAccountIdAndParamHash(accountId, paramHash)
                .map(ExportCacheEntity::getPdfData)
                .orElseGet(() -> {
                    List<TransactionEntity> txns =
                            transactionQueryRepository.findByAccount_AccountIdAndTimestampBetweenOrderByTimestampAsc(
                                    accountId, effectiveStart, effectiveEnd);
                    byte[] pdfBytes = pdfStatementService.buildPdf(accountId, effectiveStart.toLocalDate(),
                            effectiveEnd.toLocalDate(), txns);

                    ExportCacheEntity cache = new ExportCacheEntity();
                    cache.setAccountId(accountId);
                    cache.setParamHash(paramHash);
                    cache.setPdfData(pdfBytes);
                    exportCacheRepository.save(cache);
                    return pdfBytes;
                });
    }

    private String computeHash(long accountId, LocalDateTime start, LocalDateTime end) {
        String input = accountId + "|" + start.toLocalDate() + "|" + end.toLocalDate();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private TransactionItemResponse toItemResponse(TransactionEntity t) {
        TransactionItemResponse item = new TransactionItemResponse();
        item.setTransactionId(t.getTransactionId());
        item.setAmount(t.getAmount());
        item.setType(t.getType().name());
        item.setStatus(t.getStatus().name());
        item.setTimestamp(t.getTimestamp());
        item.setDescription(t.getDescription());
        item.setIdempotencyKey(t.getIdempotencyKey());
        item.setCategory(t.getCategory());
        return item;
    }
}
