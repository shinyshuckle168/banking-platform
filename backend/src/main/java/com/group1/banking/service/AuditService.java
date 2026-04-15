package com.group1.banking.service;

import com.group1.banking.entity.AuditLogEntity;
import com.group1.banking.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

/**
 * Audit service for logging all significant operations. (T012)
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String actorId, String actorRole, String action,
                    String resourceType, String resourceId, String outcome) {
        AuditLogEntity entry = new AuditLogEntity();
        entry.setActorId(actorId);
        entry.setActorRole(actorRole);
        entry.setAction(action);
        entry.setResourceType(resourceType);
        entry.setResourceId(resourceId);
        entry.setOutcome(outcome);
        auditLogRepository.save(entry);
    }
}
