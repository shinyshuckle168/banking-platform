package com.fdm.banking.service;

import com.fdm.banking.entity.*;
import com.fdm.banking.exception.*;
import com.fdm.banking.repository.AccountRepository;
import com.fdm.banking.repository.ExportCacheRepository;
import com.fdm.banking.repository.TransactionQueryRepository;
import com.fdm.banking.security.OwnershipValidator;
import com.fdm.banking.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MonthlyStatementService — dynamic PDF generation. (T084)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MonthlyStatementServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionQueryRepository transactionQueryRepository;
    @Mock private ExportCacheRepository exportCacheRepository;
    @Mock private AuditService auditService;
    @Mock private OwnershipValidator ownershipValidator;
    @Mock private PdfStatementService pdfStatementService;

    @InjectMocks
    private MonthlyStatementService service;

    private AccountEntity account;
    private UserPrincipal caller;

    @BeforeEach
    void setUp() {
        CustomerEntity customer = new CustomerEntity();
        customer.setFirstName("Jane");
        customer.setLastName("Doe");

        account = new AccountEntity();
        account.setAccountId(1L);
        account.setAccountNumber("ACC-001");
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("1000.00"));
        account.setCustomer(customer);

        caller = new UserPrincipal(10L, "user", "CUSTOMER", List.of("STATEMENT:READ"), 42L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionQueryRepository
                .findByAccount_AccountIdAndTimestampBetweenOrderByTimestampAsc(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(exportCacheRepository.findByAccountIdAndParamHash(anyLong(), anyString()))
                .thenReturn(Optional.empty());
        when(pdfStatementService.buildStatementPdf(anyLong(), any(), any(), any(), any(),
                any(), anyBoolean(), any(), any(), any(), any(), any()))
                .thenReturn(new byte[]{1, 2, 3});
        when(exportCacheRepository.save(any())).thenReturn(new ExportCacheEntity());
    }

    @Test
    void missingPermission_throwsPermissionDenied() {
        UserPrincipal noPerms = new UserPrincipal(10L, "user", "CUSTOMER", List.of(), 42L);
        assertThatThrownBy(() -> service.generateStatement(1L, "2024-01", noPerms))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void invalidPeriodFormat_throwsSemanticValidation() {
        assertThatThrownBy(() -> service.generateStatement(1L, "2024/01", caller))
                .isInstanceOf(SemanticValidationException.class);
    }

    @Test
    void futurePeriod_throwsBusinessStateException() {
        assertThatThrownBy(() -> service.generateStatement(1L, "2099-12", caller))
                .isInstanceOf(BusinessStateException.class);
    }

    @Test
    void accountNotFound_throwsResourceNotFoundException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.generateStatement(99L, "2024-01", caller))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void validRequest_returnsPdfBytes() {
        byte[] result = service.generateStatement(1L, "2024-01", caller);
        assertThat(result).isNotEmpty();
    }

    @Test
    void cachedResult_returnsCachedPdf() {
        byte[] cachedPdf = new byte[]{10, 20, 30};
        ExportCacheEntity cached = new ExportCacheEntity();
        cached.setPdfData(cachedPdf);
        when(exportCacheRepository.findByAccountIdAndParamHash(anyLong(), anyString()))
                .thenReturn(Optional.of(cached));

        byte[] result = service.generateStatement(1L, "2024-01", caller);
        assertThat(result).isEqualTo(cachedPdf);
        verify(pdfStatementService, never()).buildStatementPdf(
                anyLong(), any(), any(), any(), any(), any(), anyBoolean(),
                any(), any(), any(), any(), any());
    }
}
