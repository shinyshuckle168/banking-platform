package com.fdm.banking.service;

import com.fdm.banking.dto.response.MonthlyStatementResponse;
import com.fdm.banking.entity.*;
import com.fdm.banking.exception.*;
import com.fdm.banking.repository.AccountRepository;
import com.fdm.banking.repository.MonthlyStatementRepository;
import com.fdm.banking.mapper.MonthlyStatementMapper;
import com.fdm.banking.security.OwnershipValidator;
import com.fdm.banking.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MonthlyStatementService. (T084)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MonthlyStatementServiceTest {

    @Mock private MonthlyStatementRepository monthlyStatementRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private AuditService auditService;
    @Mock private OwnershipValidator ownershipValidator;
    @Mock private MonthlyStatementMapper monthlyStatementMapper;

    @InjectMocks
    private MonthlyStatementService service;

    private AccountEntity account;
    private UserPrincipal caller;

    @BeforeEach
    void setUp() {
        account = new AccountEntity();
        account.setAccountId(1L);

        caller = new UserPrincipal(10L, "user", "CUSTOMER", List.of("STATEMENT:READ"), 42L);
    }

    @Test
    void missingPermission_throwsPermissionDenied() {
        UserPrincipal noPerms = new UserPrincipal(10L, "user", "CUSTOMER", List.of(), 42L);
        assertThatThrownBy(() -> service.getStatement(1L, "2024-01", null, noPerms))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void invalidPeriodFormat_throwsSemanticValidation() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        assertThatThrownBy(() -> service.getStatement(1L, "2024/01", null, caller))
                .isInstanceOf(SemanticValidationException.class);
    }

    @Test
    void currentOrFuturePeriod_throwsBusinessStateException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        // Use a future period
        assertThatThrownBy(() -> service.getStatement(1L, "2099-12", null, caller))
                .isInstanceOf(BusinessStateException.class);
    }

    @Test
    void statementNotFound_throwsResourceNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(monthlyStatementRepository.findTopByAccountIdAndPeriodOrderByVersionNumberDesc(1L, "2020-01"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatement(1L, "2020-01", null, caller))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
