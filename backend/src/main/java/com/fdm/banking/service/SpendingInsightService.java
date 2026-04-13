package com.fdm.banking.service;

import com.fdm.banking.dto.response.RecategoriseResponse;
import com.fdm.banking.dto.response.SpendingInsightResponse;
import com.fdm.banking.dto.response.SpendingInsightResponse.CategoryBreakdownItem;
import com.fdm.banking.dto.response.SpendingInsightResponse.MonthTrendItem;
import com.fdm.banking.dto.response.SpendingInsightResponse.PeriodInfo;
import com.fdm.banking.dto.response.TransactionItemResponse;
import com.fdm.banking.entity.*;
import com.fdm.banking.exception.*;
import com.fdm.banking.repository.AccountRepository;
import com.fdm.banking.repository.TransactionQueryRepository;
import com.fdm.banking.security.OwnershipValidator;
import com.fdm.banking.security.UserPrincipal;
import com.fdm.banking.util.CategoryResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spending insight service. (T097, T098)
 */
@Service
public class SpendingInsightService {

    private static final List<String> VALID_CATEGORIES = List.of(
            "Housing", "Transport", "Food & Drink", "Entertainment",
            "Shopping", "Utilities", "Health", "Income");

    private final TransactionQueryRepository transactionQueryRepository;
    private final AccountRepository accountRepository;
    private final OwnershipValidator ownershipValidator;
    private final CategoryResolver categoryResolver;
    private final AuditService auditService;

    public SpendingInsightService(TransactionQueryRepository transactionQueryRepository,
                                   AccountRepository accountRepository,
                                   OwnershipValidator ownershipValidator,
                                   CategoryResolver categoryResolver,
                                   AuditService auditService) {
        this.transactionQueryRepository = transactionQueryRepository;
        this.accountRepository = accountRepository;
        this.ownershipValidator = ownershipValidator;
        this.categoryResolver = categoryResolver;
        this.auditService = auditService;
    }

    /**
     * Retrieves categorised spending insights for a given month. (T097)
     */
    @Transactional
    public SpendingInsightResponse getInsights(long accountId, int year, int month, UserPrincipal caller) {
        if (!caller.hasPermission("INSIGHTS:READ")) {
            throw new PermissionDeniedException("INSIGHTS:READ");
        }
        ownershipValidator.assertOwnership(accountId, caller);

        // Validate month
        if (month < 1 || month > 12) {
            throw new BusinessStateException("Invalid month value", "ERR_INVALID_MONTH", "month");
        }

        // Validate not future month
        YearMonth requestedMonth = YearMonth.of(year, month);
        YearMonth currentMonth = YearMonth.now();
        if (requestedMonth.isAfter(currentMonth)) {
            throw new BusinessStateException("Cannot retrieve insights for a future month",
                    "ERR_FUTURE_MONTH", "month");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found", "ERR_ACC_NOT_FOUND"));

        // Compute month range
        LocalDateTime monthStart = requestedMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = requestedMonth.atEndOfMonth().atTime(23, 59, 59);

        // Fetch eligible transactions (WITHDRAW and TRANSFER, SUCCESS only)
        List<Transaction> eligible = transactionQueryRepository.findEligibleForInsights(
            accountId, List.of(TransactionDirection.DEBIT, TransactionDirection.TRANSFER),
            TransactionStatus.SUCCESS, monthStart, monthEnd);

        // Auto-categorise uncategorised transactions (write back only if null)
        boolean hasUncategorised = false;
        for (Transaction t : eligible) {
            if (t.getCategory() == null) {
                String resolved = categoryResolver.resolve(t.getDescription());
                if (resolved != null) {
                    t.setCategory(resolved);
                    transactionQueryRepository.save(t);
                } else {
                    hasUncategorised = true;
                }
            }
        }

        // Aggregate by category
        BigDecimal totalDebitSpend = eligible.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryBreakdownItem> breakdown = buildBreakdown(eligible, totalDebitSpend);

        // Top transactions (up to 5)
        List<TransactionItemResponse> topTransactions = eligible.stream()
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .limit(5)
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        // Six-month trend
        List<MonthTrendItem> trend = buildSixMonthTrend(accountId, requestedMonth, account);

        // Period info
        PeriodInfo periodInfo = new PeriodInfo();
        periodInfo.setYear(year);
        periodInfo.setMonth(month);
        periodInfo.setComplete(requestedMonth.isBefore(currentMonth));

        SpendingInsightResponse response = new SpendingInsightResponse();
        response.setAccountId(accountId);
        response.setPeriod(periodInfo);
        response.setTotalDebitSpend(totalDebitSpend.setScale(2, RoundingMode.HALF_UP));
        response.setTransactionCount(eligible.size());
        response.setHasUncategorised(hasUncategorised);
        response.setHasExcludedDisputes(false);
        response.setDataFresh(true);
        response.setCategoryBreakdown(breakdown);
        response.setTopTransactions(topTransactions);
        response.setSixMonthTrend(trend);

        auditService.log(caller.getUserId(), caller.getRole(),
                "INSIGHTS_READ", "ACCOUNT", String.valueOf(accountId), "SUCCESS");

        return response;
    }

    /**
     * Recategorises a single transaction. (T098)
     */
    @Transactional
    public RecategoriseResponse recategorise(long accountId, long transactionId,
                                              String category, UserPrincipal caller) {
        if (!caller.hasPermission("INSIGHTS:READ")) {
            throw new PermissionDeniedException("INSIGHTS:READ");
        }
        ownershipValidator.assertOwnership(accountId, caller);

        // Validate category
        if (!VALID_CATEGORIES.contains(category)) {
            throw new SemanticValidationException(
                    "Invalid category. Must be one of: " + VALID_CATEGORIES,
                    "ERR_INVALID_CATEGORY", "category");
        }

        Transaction transaction = transactionQueryRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found: " + transactionId, "ERR_TX_NOT_FOUND"));

        String previousCategory = transaction.getCategory();
        transaction.setCategory(category);
        transactionQueryRepository.save(transaction);

        // Re-run aggregation for the transaction's month
        YearMonth txMonth = YearMonth.from(transaction.getTimestamp().toLocalDate());
        LocalDateTime monthStart = txMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = txMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> eligible = transactionQueryRepository.findEligibleForInsights(
            accountId, List.of(TransactionDirection.DEBIT, TransactionDirection.TRANSFER),
            TransactionStatus.SUCCESS, monthStart, monthEnd);

        BigDecimal totalDebitSpend = eligible.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryBreakdownItem> breakdown = buildBreakdown(eligible, totalDebitSpend);

        auditService.log(caller.getUserId(), caller.getRole(),
                "TRANSACTION_RECATEGORISE", "TRANSACTION", String.valueOf(transactionId), "SUCCESS");

        RecategoriseResponse response = new RecategoriseResponse();
        response.setTransactionId(transactionId);
        response.setPreviousCategory(previousCategory);
        response.setUpdatedCategory(category);
        response.setUpdatedTotalDebitSpend(totalDebitSpend.setScale(2, RoundingMode.HALF_UP));
        response.setUpdatedCategoryBreakdown(breakdown);
        return response;
    }

    private List<CategoryBreakdownItem> buildBreakdown(List<Transaction> eligible,
                                                         BigDecimal totalDebitSpend) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String cat : VALID_CATEGORIES) {
            totals.put(cat, BigDecimal.ZERO);
            counts.put(cat, 0);
        }

        for (Transaction t : eligible) {
            String cat = t.getCategory();
            if (cat != null && totals.containsKey(cat)) {
                totals.put(cat, totals.get(cat).add(t.getAmount()));
                counts.put(cat, counts.get(cat) + 1);
            }
        }

        return VALID_CATEGORIES.stream().map(cat -> {
            CategoryBreakdownItem item = new CategoryBreakdownItem();
            item.setCategory(cat);
            BigDecimal amount = totals.get(cat);
            item.setTotalAmount(amount.setScale(2, RoundingMode.HALF_UP));
            item.setTransactionCount(counts.get(cat));
            BigDecimal pct = (totalDebitSpend.compareTo(BigDecimal.ZERO) == 0)
                    ? BigDecimal.ZERO
                    : amount.multiply(new BigDecimal("100"))
                            .divide(totalDebitSpend, 2, RoundingMode.HALF_UP);
            item.setPercentage(pct);
            return item;
        }).collect(Collectors.toList());
    }

    private List<MonthTrendItem> buildSixMonthTrend(long accountId, YearMonth requestedMonth,
                                                      Account account) {
        List<MonthTrendItem> trend = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth trendMonth = requestedMonth.minusMonths(i);
            MonthTrendItem item = new MonthTrendItem();
            item.setYear(trendMonth.getYear());
            item.setMonth(trendMonth.getMonthValue());
            item.setComplete(trendMonth.isBefore(currentMonth));

            LocalDate accountCreated = account.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
            boolean existed = !trendMonth.isBefore(YearMonth.from(accountCreated));
            item.setAccountExisted(existed);

            if (!existed) {
                item.setTotalSpend(BigDecimal.ZERO);
            } else {
                LocalDateTime mStart = trendMonth.atDay(1).atStartOfDay();
                LocalDateTime mEnd = trendMonth.atEndOfMonth().atTime(23, 59, 59);
                List<Transaction> txns = transactionQueryRepository.findEligibleForInsights(
                    accountId, List.of(TransactionDirection.DEBIT, TransactionDirection.TRANSFER),
                    TransactionStatus.SUCCESS, mStart, mEnd);
                BigDecimal total = txns.stream().map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                item.setTotalSpend(total.setScale(2, RoundingMode.HALF_UP));
            }
            trend.add(item);
        }
        return trend;
    }

    private TransactionItemResponse toItemResponse(Transaction t) {
        TransactionItemResponse item = new TransactionItemResponse();
        item.setTransactionId(t.getTransactionId());
        item.setAmount(t.getAmount());
        item.setDirection(t.getDirection() != null ? t.getDirection().name() : null);
        item.setStatus(t.getStatus() != null ? t.getStatus().name() : null);
        item.setTimestamp(t.getTimestamp());
        item.setDescription(t.getDescription());
        item.setIdempotencyKey(t.getIdempotencyKey());
        item.setCategory(t.getCategory());
        item.setSenderInfo(t.getSenderInfo());
        item.setReceiverInfo(t.getReceiverInfo());
        item.setExternalTransactionId(t.getExternalTransactionId());
        return item;
    }
}
