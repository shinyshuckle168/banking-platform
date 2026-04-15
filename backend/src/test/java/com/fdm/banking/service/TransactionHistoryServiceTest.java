//package com.fdm.banking.service;
//
//import com.fdm.banking.dto.response.TransactionHistoryResponse;
//import com.fdm.banking.entity.*;
//import com.fdm.banking.exception.*;
//import com.fdm.banking.repository.AccountRepository;
//import com.fdm.banking.repository.ExportCacheRepository;
//import com.fdm.banking.repository.TransactionQueryRepository;
//import com.fdm.banking.security.OwnershipValidator;
//import com.fdm.banking.security.UserPrincipal;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for TransactionHistoryService. (T030)
// */
//@ExtendWith(MockitoExtension.class)
//class TransactionHistoryServiceTest {
//
//    @Mock private TransactionQueryRepository transactionQueryRepository;
//    @Mock private AccountRepository accountRepository;
//    @Mock private ExportCacheRepository exportCacheRepository;
//    @Mock private AuditService auditService;
//    @Mock private OwnershipValidator ownershipValidator;
//
//    @InjectMocks
//    private TransactionHistoryService service;
//
//    private Account account;
//    private UserPrincipal caller;
//
//    @BeforeEach
//    void setUp() {
//        account = new Account();
//        account.setAccountId(1L);
//        account.setStatus(AccountStatus.ACTIVE);
//
//        caller = new UserPrincipal("10", "user", List.of("CUSTOMER"), List.of("TRANSACTION:READ"), 42L);
//    }
//
//    @Test
//    void missingPermission_throwsPermissionDenied() {
//        UserPrincipal noPerms = new UserPrincipal("10", "user", List.of("CUSTOMER"), List.of(), 42L);
//        assertThatThrownBy(() -> service.getHistory(1L, null, null, noPerms))
//                .isInstanceOf(PermissionDeniedException.class);
//    }
//
//    @Test
//    void future_endDate_isOverriddenToNow() {
//        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
//        when(transactionQueryRepository.findByAccount_AccountIdAndTimestampBetweenOrderByTimestampAsc(
//                anyLong(), any(), any())).thenReturn(Collections.emptyList());
//
//        LocalDate futureEnd = LocalDate.now().plusDays(5);
//        // Should not throw; endDate is overridden
//        TransactionHistoryResponse resp = service.getHistory(1L, null, futureEnd, caller);
//        assertThat(resp).isNotNull();
//    }
//
//    @Test
//    void closedAccount_exceedingWindow_throwsRetentionWindowException() {
//        account.setStatus(AccountStatus.CLOSED);
//        account.setClosedAt(Instant.now().minus(100, ChronoUnit.DAYS));
//        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
//
//        assertThatThrownBy(() -> service.getHistory(1L, null, null, caller))
//                .isInstanceOf(RetentionWindowException.class);
//    }
//
//    @Test
//    void accountNotFound_throwsResourceNotFoundException() {
//        when(accountRepository.findById(999L)).thenReturn(Optional.empty());
//        assertThatThrownBy(() -> service.getHistory(999L, null, null, caller))
//                .isInstanceOf(ResourceNotFoundException.class);
//    }
//
//    @Test
//    void validRequest_returnsTransactionHistoryResponse() {
//        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
//        when(transactionQueryRepository.findByAccount_AccountIdAndTimestampBetweenOrderByTimestampAsc(
//                anyLong(), any(), any())).thenReturn(Collections.emptyList());
//
//        TransactionHistoryResponse resp = service.getHistory(1L, null, null, caller);
//        assertThat(resp).isNotNull();
//        assertThat(resp.getAccountId()).isEqualTo(1L);
//    }
//}
