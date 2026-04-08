package com.fdm.banking.scheduler;

import com.fdm.banking.entity.*;
import com.fdm.banking.repository.*;
import com.fdm.banking.service.AuditService;
import com.fdm.banking.service.NotificationEvaluationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Standing order execution scheduler. (T054, T055)
 * Processes daily standing orders, handles retries, and triggers notifications on failure.
 */
@Component
public class StandingOrderExecutionJob {

    private final StandingOrderRepository standingOrderRepository;
    private final TransactionQueryRepository transactionQueryRepository;
    private final AccountRepository accountRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final NotificationEvaluationService notificationEvaluationService;
    private final com.fdm.banking.util.CanadianHolidayService canadianHolidayService;
    private final AuditService auditService;

    public StandingOrderExecutionJob(StandingOrderRepository standingOrderRepository,
                                      TransactionQueryRepository transactionQueryRepository,
                                      AccountRepository accountRepository,
                                      IdempotencyRecordRepository idempotencyRecordRepository,
                                      NotificationEvaluationService notificationEvaluationService,
                                      com.fdm.banking.util.CanadianHolidayService canadianHolidayService,
                                      AuditService auditService) {
        this.standingOrderRepository = standingOrderRepository;
        this.transactionQueryRepository = transactionQueryRepository;
        this.accountRepository = accountRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.notificationEvaluationService = notificationEvaluationService;
        this.canadianHolidayService = canadianHolidayService;
        this.auditService = auditService;
    }

    /**
     * Main daily execution at 00:00:01 UTC — processes orders due today. (T054)
     */
    @Scheduled(cron = "1 0 0 * * *", zone = "UTC")
    @Transactional
    public void processOrders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        List<StandingOrderEntity> dueOrders = standingOrderRepository
                .findByStatusAndNextRunDateBefore(StandingOrderStatus.ACTIVE,
                        today.plusDays(1).atStartOfDay());

        for (StandingOrderEntity order : dueOrders) {
            if (order.getNextRunDate().toLocalDate().equals(today)) {
                attemptExecution(order);
            }
        }
    }

    /**
     * First retry attempt at 08:00 UTC — processes RETRY_PENDING orders. (T054)
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "UTC")
    @Transactional
    public void firstAttempt() {
        List<StandingOrderEntity> retryOrders =
                standingOrderRepository.findByStatus(StandingOrderStatus.RETRY_PENDING);
        for (StandingOrderEntity order : retryOrders) {
            if (order.getNextRunDate().toLocalDate().equals(LocalDate.now())) {
                attemptExecution(order);
            }
        }
    }

    /**
     * Final retry attempt at 16:00 UTC — executes remaining RETRY_PENDING. (T054)
     */
    @Scheduled(cron = "0 0 16 * * *", zone = "UTC")
    @Transactional
    public void finalAttempt() {
        List<StandingOrderEntity> retryOrders =
                standingOrderRepository.findByStatus(StandingOrderStatus.RETRY_PENDING);
        for (StandingOrderEntity order : retryOrders) {
            if (order.getNextRunDate().toLocalDate().equals(LocalDate.now())) {
                finalExecutionAttempt(order);
            }
        }
    }

    /**
     * Attempts to execute a standing order. (T055)
     */
    public void attemptExecution(StandingOrderEntity order) {
        String idempotencyKey = order.getStandingOrderId() + ":" + order.getNextRunDate().toLocalDate();

        // Idempotency check
        if (idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            return; // Already processed this cycle
        }

        AccountEntity account = accountRepository.findById(order.getSourceAccountId()).orElse(null);
        if (account == null) {
            order.setStatus(StandingOrderStatus.TERMINATED);
            standingOrderRepository.save(order);
            return;
        }

        // Check balance sufficiency
        if (account.getBalance().compareTo(order.getAmount()) < 0) {
            order.setStatus(StandingOrderStatus.RETRY_PENDING);
            standingOrderRepository.save(order);
            return;
        }

        // Execute transfer
        executeTransfer(account, order, idempotencyKey);
    }

    private void finalExecutionAttempt(StandingOrderEntity order) {
        String idempotencyKey = order.getStandingOrderId() + ":" + order.getNextRunDate().toLocalDate();

        AccountEntity account = accountRepository.findById(order.getSourceAccountId()).orElse(null);
        if (account == null) {
            order.setStatus(StandingOrderStatus.TERMINATED);
            standingOrderRepository.save(order);
            return;
        }

        if (account.getBalance().compareTo(order.getAmount()) < 0) {
            // Final failure — create FAILED transaction
            TransactionEntity failedTx = new TransactionEntity();
            failedTx.setAccount(account);
            failedTx.setAmount(order.getAmount());
            failedTx.setType(TransactionType.TRANSFER);
            failedTx.setStatus(TransactionStatus.FAILED);
            failedTx.setDescription("Standing order FAILED: " + order.getStandingOrderId() +
                    " — insufficient funds");
            failedTx.setIdempotencyKey(idempotencyKey);
            transactionQueryRepository.save(failedTx);

            order.setStatus(StandingOrderStatus.FAILED_INSUFFICIENT_FUNDS);
            standingOrderRepository.save(order);

            // Trigger notification
            try {
                notificationEvaluationService.evaluateInternal(
                        "StandingOrderFailure",
                        account.getAccountId(),
                        account.getCustomer().getCustomerId(),
                        LocalDateTime.now().toString(),
                        "Standing order " + order.getStandingOrderId() + " failed — insufficient funds"
                );
            } catch (Exception e) {
                // Log but do not fail the scheduler
                auditService.log(-1L, "SYSTEM", "NOTIFICATION_FAILED",
                        "STANDING_ORDER", order.getStandingOrderId(), "ERROR");
            }
            return;
        }

        executeTransfer(account, order, idempotencyKey);
    }

    private void executeTransfer(AccountEntity account, StandingOrderEntity order, String idempotencyKey) {
        // Deduct balance
        account.setBalance(account.getBalance().subtract(order.getAmount()));
        accountRepository.save(account);

        // Create SUCCESS transaction
        TransactionEntity tx = new TransactionEntity();
        tx.setAccount(account);
        tx.setAmount(order.getAmount());
        tx.setType(TransactionType.TRANSFER);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setDescription("Standing order: " + order.getPayeeName() + " - " + order.getReference());
        tx.setIdempotencyKey(idempotencyKey);
        transactionQueryRepository.save(tx);

        // Store idempotency record
        IdempotencyRecordEntity record = new IdempotencyRecordEntity();
        record.setIdempotencyKey(idempotencyKey);
        record.setResponseBody("SUCCESS");
        record.setStatusCode(200);
        idempotencyRecordRepository.save(record);

        // Advance nextRunDate
        LocalDateTime nextRun = calculateNextRun(order);
        order.setNextRunDate(nextRun);
        order.setStatus(StandingOrderStatus.ACTIVE);
        standingOrderRepository.save(order);

        auditService.log(-1L, "SYSTEM", "STANDING_ORDER_EXECUTED",
                "STANDING_ORDER", order.getStandingOrderId(), "SUCCESS");
    }

    private LocalDateTime calculateNextRun(StandingOrderEntity order) {
        LocalDateTime current = order.getNextRunDate();
        LocalDateTime next = switch (order.getFrequency()) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case QUARTERLY -> current.plusMonths(3);
        };
        return canadianHolidayService.nextBusinessDay(next);
    }
}
