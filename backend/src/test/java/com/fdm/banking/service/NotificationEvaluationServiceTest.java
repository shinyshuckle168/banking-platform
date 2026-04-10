package com.fdm.banking.service;

import com.fdm.banking.dto.request.NotificationEventRequest;
import com.fdm.banking.dto.response.NotificationDecisionResponse;
import com.fdm.banking.entity.*;
import com.fdm.banking.exception.*;
import com.fdm.banking.repository.AccountRepository;
import com.fdm.banking.repository.NotificationDecisionRepository;
import com.fdm.banking.repository.NotificationPreferenceRepository;
import com.fdm.banking.mapper.NotificationDecisionMapper;
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
 * Unit tests for NotificationEvaluationService. (T076)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationEvaluationServiceTest {

    @Mock private NotificationDecisionRepository notificationDecisionRepository;
    @Mock private NotificationPreferenceRepository notificationPreferenceRepository;
    @Mock private NotificationDecisionMapper notificationDecisionMapper;
    @Mock private AccountRepository accountRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private NotificationEvaluationService service;

    private Account account;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setCustomerId(42L);

        account = new Account();
        account.setAccountId(1L);
        account.setCustomer(customer);

        when(notificationDecisionMapper.toResponse(any())).thenCallRealMethod();
    }

    @Test
    void duplicateEventId_throwsBusinessStateException() {
        when(notificationDecisionRepository.existsByEventId("evt001")).thenReturn(true);

        NotificationEventRequest req = buildRequest("evt001", "StandingOrderFailure");
        assertThatThrownBy(() -> service.evaluate(req))
                .isInstanceOf(BusinessStateException.class);
    }

    @Test
    void unknownEventType_throwsSemanticValidationException() {
        when(notificationDecisionRepository.existsByEventId("evt002")).thenReturn(false);

        NotificationEventRequest req = buildRequest("evt002", "UnknownEvent");
        assertThatThrownBy(() -> service.evaluate(req))
                .isInstanceOf(SemanticValidationException.class);
    }

    @Test
    void mandatoryEvent_alwaysRaised() {
        when(notificationDecisionRepository.existsByEventId("evt003")).thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        NotificationEventRequest req = buildRequest("evt003", "StandingOrderFailure");
        NotificationDecisionResponse result = service.evaluate(req);
        assertThat(result.getDecision()).isEqualTo("RAISED");
    }

    @Test
    void optionalEvent_preferenceEnabled_raised() {
        when(notificationDecisionRepository.existsByEventId("evt004")).thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        NotificationPreferenceEntity pref = new NotificationPreferenceEntity();
        pref.setOptedIn(true);
        when(notificationPreferenceRepository.findByCustomerIdAndEventType(42L, "StatementAvailability"))
                .thenReturn(Optional.of(pref));

        NotificationEventRequest req = buildRequest("evt004", "StatementAvailability");
        NotificationDecisionResponse result = service.evaluate(req);
        assertThat(result.getDecision()).isEqualTo("RAISED");
    }

    @Test
    void optionalEvent_preferenceDisabled_suppressed() {
        when(notificationDecisionRepository.existsByEventId("evt005")).thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        NotificationPreferenceEntity pref = new NotificationPreferenceEntity();
        pref.setOptedIn(false);
        when(notificationPreferenceRepository.findByCustomerIdAndEventType(42L, "StatementAvailability"))
                .thenReturn(Optional.of(pref));

        NotificationEventRequest req = buildRequest("evt005", "StatementAvailability");
        NotificationDecisionResponse result = service.evaluate(req);
        assertThat(result.getDecision()).isEqualTo("SUPPRESSED");
    }

    private NotificationEventRequest buildRequest(String eventId, String eventType) {
        NotificationEventRequest req = new NotificationEventRequest();
        req.setEventId(eventId);
        req.setEventType(eventType);
        req.setAccountId(1L);
        req.setCustomerId(42L);
        return req;
    }
}
