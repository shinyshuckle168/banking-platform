package com.fdm.banking.service;

import com.fdm.banking.dto.response.SpendingInsightResponse;
import com.fdm.banking.entity.*;
import com.fdm.banking.exception.*;
import com.fdm.banking.repository.AccountRepository;
import com.fdm.banking.repository.TransactionQueryRepository;
import com.fdm.banking.security.OwnershipValidator;
import com.fdm.banking.security.UserPrincipal;
import com.fdm.banking.util.CategoryResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SpendingInsightService. (T101)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpendingInsightServiceTest {

    @Mock private TransactionQueryRepository transactionQueryRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private OwnershipValidator ownershipValidator;
    @Mock private CategoryResolver categoryResolver;
    @Mock private AuditService auditService;

    @InjectMocks
    private SpendingInsightService service;

    private Account account;
    private UserPrincipal caller;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setAccountId(1L);
        account.setCreatedAt(Instant.now().minus(2 * 365, ChronoUnit.DAYS));

        caller = new UserPrincipal("10", "user", List.of("CUSTOMER"), List.of("INSIGHTS:READ"), 42L);
    }

    @Test
    void missingPermission_throwsPermissionDenied() {
        UserPrincipal noPerms = new UserPrincipal("10", "user", List.of("CUSTOMER"), List.of(), 42L);
        assertThatThrownBy(() -> service.getInsights(1L, 2024, 1, noPerms))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void futureMonth_throwsBusinessStateException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        assertThatThrownBy(() -> service.getInsights(1L, 2099, 12, caller))
                .isInstanceOf(BusinessStateException.class)
                .hasMessageContaining("future");
    }

    @Test
    void validRequest_returnsAllEightCategories() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionQueryRepository.findEligibleForInsights(anyLong(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        SpendingInsightResponse resp = service.getInsights(1L, 2024, 1, caller);
        assertThat(resp.getCategoryBreakdown()).hasSize(8);
    }

    @Test
    void allCategoriesPresent_evenWhenNoTransactions() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionQueryRepository.findEligibleForInsights(anyLong(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        SpendingInsightResponse resp = service.getInsights(1L, 2024, 1, caller);
        List<String> categories = resp.getCategoryBreakdown().stream()
                .map(SpendingInsightResponse.CategoryBreakdownItem::getCategory)
                .toList();
        assertThat(categories).containsExactlyInAnyOrder(
                "Housing", "Transport", "Food & Drink", "Entertainment",
                "Shopping", "Utilities", "Health", "Income");
    }

    @Test
    void recategorise_invalidCategory_throwsSemanticValidation() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        assertThatThrownBy(() -> service.recategorise(1L, 1L, "InvalidCategory", caller))
                .isInstanceOf(SemanticValidationException.class);
    }

    @Test
    void recategorise_transactionNotFound_throwsResourceNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionQueryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recategorise(1L, 99L, "Shopping", caller))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
