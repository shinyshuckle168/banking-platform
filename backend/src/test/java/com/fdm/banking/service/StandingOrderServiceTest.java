package com.fdm.banking.service;

import com.fdm.banking.dto.request.CreateStandingOrderRequest;
import com.fdm.banking.dto.response.StandingOrderResponse;
import com.fdm.banking.entity.*;
import com.fdm.banking.exception.*;
import com.fdm.banking.repository.AccountRepository;
import com.fdm.banking.repository.StandingOrderRepository;
import com.fdm.banking.security.OwnershipValidator;
import com.fdm.banking.security.UserPrincipal;
import com.fdm.banking.util.CanadianHolidayService;
import com.fdm.banking.mapper.StandingOrderMapper;
import com.fdm.banking.util.Mod97Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StandingOrderService. (T056)
 */
@ExtendWith(MockitoExtension.class)
class StandingOrderServiceTest {

    @Mock private StandingOrderRepository standingOrderRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private OwnershipValidator ownershipValidator;
    @Mock private CanadianHolidayService canadianHolidayService;
    @Mock private Mod97Validator mod97Validator;
    @Mock private StandingOrderMapper mapper;
    @Mock private AuditService auditService;

    @InjectMocks
    private StandingOrderService service;

    private Account account;
    private UserPrincipal caller;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setAccountId(1L);
        account.setBalance(new BigDecimal("5000.00"));
        account.setDailyTransferLimit(new BigDecimal("3000.00"));
        account.setStatus(AccountStatus.ACTIVE);
        Customer customer = new Customer();
        customer.setCustomerId(42L);
        account.setCustomer(customer);

        caller = new UserPrincipal("10", "user", List.of("CUSTOMER"),
                List.of("SO:CREATE", "SO:READ", "SO:CANCEL"), 42L);
    }

    @Test
    void create_missingPermission_throwsPermissionDenied() {
        UserPrincipal noPerms = new UserPrincipal("10", "user", List.of("CUSTOMER"), List.of(), 42L);
        assertThatThrownBy(() -> service.create(1L, new CreateStandingOrderRequest(), noPerms))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void create_invalidPayee_throwsSemanticValidation() {
        CreateStandingOrderRequest req = validRequest();
        req.setPayeeAccount("GB00WEST12345698765432"); // invalid checksum
        assertThatThrownBy(() -> service.create(1L, req, caller))
                .isInstanceOf(SemanticValidationException.class);
    }

    @Test
    void create_startDateTooSoon_throwsSemanticValidation() {
        CreateStandingOrderRequest req = validRequest();
        req.setStartDate(LocalDateTime.now().plusHours(1));
        assertThatThrownBy(() -> service.create(1L, req, caller))
                .isInstanceOf(SemanticValidationException.class);
    }

    @Test
    void list_returnsOrders() {
        when(standingOrderRepository.findBySourceAccountId(1L)).thenReturn(Collections.emptyList());

        assertThatCode(() -> service.list(1L, caller)).doesNotThrowAnyException();
    }

    @Test
    void cancel_orderNotFound_throwsResourceNotFound() {
        when(standingOrderRepository.findById("order-99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cancel("order-99", caller))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private CreateStandingOrderRequest validRequest() {
        CreateStandingOrderRequest req = new CreateStandingOrderRequest();
        req.setPayeeAccount("GB82WEST12345698765432");
        req.setAmount(new BigDecimal("100.00"));
        req.setFrequency("MONTHLY");
        req.setReference("Test standing order");
        req.setStartDate(LocalDateTime.now().plusDays(2));
        return req;
    }
}
