package com.group1.banking.service;

import com.group1.banking.dto.request.CreateStandingOrderRequest;
import com.group1.banking.dto.response.CancelStandingOrderResponse;
import com.group1.banking.dto.response.StandingOrderListResponse;
import com.group1.banking.dto.response.StandingOrderResponse;
import com.group1.banking.entity.*;
import com.group1.banking.exception.*;
import com.group1.banking.mapper.StandingOrderMapper;
import com.group1.banking.repository.AccountRepository;
import com.group1.banking.repository.StandingOrderRepository;
import com.group1.banking.security.OwnershipValidator;
import com.group1.banking.security.UserPrincipal;
import com.group1.banking.util.CanadianHolidayService;
import com.group1.banking.util.Mod97Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Standing order management service. (T050, T051, T052)
 */
@Service
public class StandingOrderService {

    private final StandingOrderRepository standingOrderRepository;
    private final AccountRepository accountRepository;
    private final OwnershipValidator ownershipValidator;
    private final CanadianHolidayService canadianHolidayService;
    private final StandingOrderMapper mapper;
    private final AuditService auditService;

    public StandingOrderService(StandingOrderRepository standingOrderRepository,
                                AccountRepository accountRepository,
                                OwnershipValidator ownershipValidator,
                                CanadianHolidayService canadianHolidayService,
                                StandingOrderMapper mapper,
                                AuditService auditService) {
        this.standingOrderRepository = standingOrderRepository;
        this.accountRepository = accountRepository;
        this.ownershipValidator = ownershipValidator;
        this.canadianHolidayService = canadianHolidayService;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    /**
     * Creates a new standing order. (T050)
     */
    @Transactional
    public StandingOrderResponse create(long accountId, CreateStandingOrderRequest req,
                                         UserPrincipal caller) {
        if (!caller.hasPermission("SO:CREATE")) {
            throw new PermissionDeniedException("SO:CREATE");
        }
        ownershipValidator.assertOwnership(accountId, caller);

        // Defence-in-depth Modulo 97 check
        if (!Mod97Validator.isValidMod97(req.getPayeeAccount())) {
            throw new SemanticValidationException("Invalid payee account checksum",
                    "ERR_CHECKSUM_FAILURE", "payeeAccount");
        }

        // Validate startDate >= now + 24h
        LocalDateTime now = LocalDateTime.now();
        if (req.getStartDate() == null || req.getStartDate().isBefore(now.plusHours(24))) {
            throw new SemanticValidationException("Start date must be at least 24 hours in the future",
                    "ERR_START_DATE_TOO_SOON", "startDate");
        }

        // Validate endDate is after startDate when provided
        if (req.getEndDate() != null && !req.getEndDate().isAfter(req.getStartDate())) {
            throw new BusinessStateException("End date must be after start date",
                    "ERR_END_DATE_BEFORE_START", "endDate");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found", "ERR_ACC_NOT_FOUND"));

        // Validate amount <= dailyTransferLimit
        if (req.getAmount().compareTo(account.getDailyTransferLimit()) > 0) {
            throw new SemanticValidationException(
                    "Amount exceeds daily transfer limit of " + account.getDailyTransferLimit(),
                    "ERR_AMOUNT_EXCEEDS_LIMIT", "amount");
        }

        // Parse frequency
        Frequency frequency;
        try {
            frequency = Frequency.valueOf(req.getFrequency().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SemanticValidationException("Invalid frequency value", "ERR_INVALID_FREQUENCY", "frequency");
        }

        // Check for duplicate ACTIVE order
        standingOrderRepository.findBySourceAccountIdAndPayeeAccountAndAmountAndFrequencyAndStatus(
                accountId, req.getPayeeAccount(), req.getAmount(), frequency, StandingOrderStatus.ACTIVE)
                .ifPresent(dup -> {
                    throw new BusinessStateException("Identical ACTIVE standing order already exists",
                            "ERR_SO_DUPLICATE");
                });

        // Calculate nextRunDate
        LocalDateTime nextRunDate = canadianHolidayService.nextBusinessDay(req.getStartDate());

        // Build entity
        StandingOrderEntity entity = new StandingOrderEntity();
        entity.setStandingOrderId(UUID.randomUUID().toString());
        entity.setSourceAccountId(accountId);
        entity.setPayeeAccount(req.getPayeeAccount());
        entity.setPayeeName(req.getPayeeName());
        entity.setAmount(req.getAmount());
        entity.setFrequency(frequency);
        entity.setStartDate(req.getStartDate());
        entity.setEndDate(req.getEndDate());
        entity.setReference(req.getReference());
        entity.setStatus(StandingOrderStatus.ACTIVE);
        entity.setNextRunDate(nextRunDate);

        standingOrderRepository.save(entity);

        auditService.log(caller.getUserId(), caller.getRole(),
                "STANDING_ORDER_CREATE", "STANDING_ORDER", entity.getStandingOrderId(), "SUCCESS");

        StandingOrderResponse response = mapper.toResponse(entity);
        response.setMessage("Standing order created successfully.");
        return response;
    }

    /**
     * Lists standing orders for an account. (T051)
     */
    public StandingOrderListResponse list(long accountId, UserPrincipal caller) {
        if (!caller.hasPermission("SO:READ")) {
            throw new PermissionDeniedException("SO:READ");
        }
        ownershipValidator.assertOwnership(accountId, caller);

        List<StandingOrderEntity> orders = standingOrderRepository.findBySourceAccountId(accountId);
        List<StandingOrderResponse> items = orders.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        auditService.log(caller.getUserId(), caller.getRole(),
                "STANDING_ORDER_LIST", "ACCOUNT", String.valueOf(accountId), "SUCCESS");

        StandingOrderListResponse response = new StandingOrderListResponse();
        response.setAccountId(accountId);
        response.setStandingOrderCount(items.size());
        response.setStandingOrders(items);
        return response;
    }

    /**
     * Cancels a standing order. (T052)
     */
    @Transactional
    public CancelStandingOrderResponse cancel(String standingOrderId, UserPrincipal caller) {
        if (!caller.hasPermission("SO:CANCEL")) {
            throw new PermissionDeniedException("SO:CANCEL");
        }

        StandingOrderEntity entity = standingOrderRepository.findById(standingOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Standing order not found: " + standingOrderId, "ERR_SO_NOT_FOUND"));

        ownershipValidator.assertOwnership(entity.getSourceAccountId(), caller);

        // Check lock window: within 24h of nextRunDate
        LocalDateTime now = LocalDateTime.now();
        if (entity.getNextRunDate() != null && entity.getNextRunDate().minusHours(24).isBefore(now)) {
            throw new LockException("Cannot cancel standing order within 24 hours of next run date",
                    "ERR_SO_LOCKED");
        }

        entity.setStatus(StandingOrderStatus.CANCELLED);
        standingOrderRepository.save(entity);

        auditService.log(caller.getUserId(), caller.getRole(),
                "STANDING_ORDER_CANCEL", "STANDING_ORDER", standingOrderId, "SUCCESS");

        return new CancelStandingOrderResponse(standingOrderId, "CANCELLED",
                "Standing order cancelled successfully.");
    }
}
