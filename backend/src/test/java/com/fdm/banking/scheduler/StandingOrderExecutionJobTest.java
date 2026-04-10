package com.fdm.banking.scheduler;

import com.fdm.banking.entity.*;
import com.fdm.banking.repository.AccountRepository;
import com.fdm.banking.repository.IdempotencyRecordRepository;
import com.fdm.banking.repository.StandingOrderRepository;
import com.fdm.banking.repository.TransactionQueryRepository;
import com.fdm.banking.service.AuditService;
import com.fdm.banking.service.NotificationEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StandingOrderExecutionJob. (T057)
 */
@ExtendWith(MockitoExtension.class)
class StandingOrderExecutionJobTest {

    @Mock private StandingOrderRepository standingOrderRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionQueryRepository transactionQueryRepository;
    @Mock private IdempotencyRecordRepository idempotencyRecordRepository;
    @Mock private NotificationEvaluationService notificationEvaluationService;
    @Mock private AuditService auditService;

    @InjectMocks
    private StandingOrderExecutionJob job;

    private StandingOrderEntity order;
    private AccountEntity account;

    @BeforeEach
    void setUp() {
        account = new AccountEntity();
        account.setAccountId(1L);
        account.setBalance(new BigDecimal("1000.00"));

        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId(42L);
        account.setCustomer(customer);

        AccountEntity payee = new AccountEntity();
        payee.setAccountId(2L);

        order = new StandingOrderEntity();
        order.setStandingOrderId("order-10");
        order.setSourceAccountId(1L);
        order.setAmount(new BigDecimal("100.00"));
        order.setStatus(StandingOrderStatus.ACTIVE);
        order.setNextRunDate(LocalDateTime.now());
        order.setReference("Test");
    }

    @Test
    void processOrders_noActive_doesNothing() {
        when(standingOrderRepository.findByStatusAndNextRunDateBefore(
                any(), any())).thenReturn(List.of());
        job.processOrders();
        verify(idempotencyRecordRepository, never()).findByIdempotencyKey(any());
    }

    @Test
    void processOrders_idempotencyKeyExists_skipsOrder() {
        when(standingOrderRepository.findByStatusAndNextRunDateBefore(any(), any()))
                .thenReturn(List.of(order));
        when(idempotencyRecordRepository.findByIdempotencyKey(any())).thenReturn(Optional.of(new IdempotencyRecordEntity()));

        job.processOrders();
        verify(transactionQueryRepository, never()).save(any());
    }

    @Test
    void processOrders_insufficientBalance_setsRetryPending() {
        account.setBalance(new BigDecimal("50.00"));
        when(standingOrderRepository.findByStatusAndNextRunDateBefore(any(), any()))
                .thenReturn(List.of(order));
        when(idempotencyRecordRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        job.processOrders();
        verify(standingOrderRepository).save(argThat(o -> o.getStatus() == StandingOrderStatus.RETRY_PENDING));
    }
}
