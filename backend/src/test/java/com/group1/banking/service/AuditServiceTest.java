package com.group1.banking.service;

import com.group1.banking.entity.AuditLogEntity;
import com.group1.banking.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuditService.
 */
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void log_shouldSaveAuditRecord_withCorrectFields() {
        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        when(auditLogRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        auditService.log("user-123", "ADMIN", "LOGIN", "USER", "user-123", "SUCCESS");

        AuditLogEntity saved = captor.getValue();
        assertThat(saved.getActorId()).isEqualTo("user-123");
        assertThat(saved.getActorRole()).isEqualTo("ADMIN");
        assertThat(saved.getAction()).isEqualTo("LOGIN");
        assertThat(saved.getResourceType()).isEqualTo("USER");
        assertThat(saved.getResourceId()).isEqualTo("user-123");
        assertThat(saved.getOutcome()).isEqualTo("SUCCESS");
    }

    @Test
    void log_shouldPersistToRepository() {
        when(auditLogRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        auditService.log("-1", "SYSTEM", "NOTIFICATION_FAILED", "NOTIFICATION", "evt-001", "ERROR");

        verify(auditLogRepository).save(org.mockito.ArgumentMatchers.any(AuditLogEntity.class));
    }

    @Test
    void log_shouldAcceptNullValues_withoutException() {
        when(auditLogRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        auditService.log(null, null, "ACTION", "TYPE", null, null);

        verify(auditLogRepository).save(org.mockito.ArgumentMatchers.any(AuditLogEntity.class));
    }
}
