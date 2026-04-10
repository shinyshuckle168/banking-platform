package com.fdm.banking.service;

import com.fdm.banking.dto.response.MonthlyStatementResponse;
import com.fdm.banking.entity.MonthlyStatementEntity;
import com.fdm.banking.exception.*;
import com.fdm.banking.mapper.MonthlyStatementMapper;
import com.fdm.banking.repository.MonthlyStatementRepository;
import com.fdm.banking.security.OwnershipValidator;
import com.fdm.banking.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Monthly statement service. (T082)
 */
@Service
public class MonthlyStatementService {

    private static final int RETENTION_YEARS = 7;
    private static final DateTimeFormatter PERIOD_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final MonthlyStatementRepository monthlyStatementRepository;
    private final OwnershipValidator ownershipValidator;
    private final MonthlyStatementMapper mapper;
    private final AuditService auditService;

    public MonthlyStatementService(MonthlyStatementRepository monthlyStatementRepository,
                                    OwnershipValidator ownershipValidator,
                                    MonthlyStatementMapper mapper,
                                    AuditService auditService) {
        this.monthlyStatementRepository = monthlyStatementRepository;
        this.ownershipValidator = ownershipValidator;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    /**
     * Retrieves a monthly statement for a closed period. (T082)
     */
    public MonthlyStatementResponse getStatement(long accountId, String period,
                                                   Integer version, UserPrincipal caller) {
        // Re-validate permission at delivery
        if (!caller.hasPermission("STATEMENT:READ")) {
            throw new PermissionDeniedException("STATEMENT:READ");
        }
        ownershipValidator.assertOwnership(accountId, caller);

        // Validate period format
        LocalDate periodStart;
        try {
            periodStart = LocalDate.parse(period + "-01",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw new SemanticValidationException("Invalid period format. Expected YYYY-MM",
                    "ERR_INVALID_PERIOD_FORMAT", "period");
        }

        // Period must be before current month
        LocalDate today = LocalDate.now();
        LocalDate firstOfCurrentMonth = today.withDayOfMonth(1);
        if (!periodStart.isBefore(firstOfCurrentMonth)) {
            throw new BusinessStateException("Period is not yet closed",
                    "ERR_PERIOD_NOT_CLOSED", "period");
        }

        // Retention window: not more than 7 years ago
        LocalDate retentionCutoff = today.minusYears(RETENTION_YEARS);
        if (periodStart.isBefore(retentionCutoff)) {
            throw new RetentionWindowException("Statement is beyond 7-year retention window",
                    "ERR_RETENTION_WINDOW_EXCEEDED");
        }

        // Resolve statement
        MonthlyStatementEntity entity;
        if (version != null) {
            entity = monthlyStatementRepository
                    .findByAccountIdAndPeriodAndVersionNumber(accountId, period, version)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Statement not found for period " + period + " version " + version,
                            "ERR_STATEMENT_NOT_FOUND"));
        } else {
            entity = monthlyStatementRepository
                    .findTopByAccountIdAndPeriodOrderByVersionNumberDesc(accountId, period)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No statement found for period " + period,
                            "ERR_STATEMENT_NOT_FOUND"));
        }

        auditService.log(caller.getUserId(), caller.getRole(),
                "STATEMENT_READ", "STATEMENT", accountId + "/" + period, "SUCCESS");

        return mapper.toResponse(entity);
    }
}
