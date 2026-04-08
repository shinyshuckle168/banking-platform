package com.bank.service;

import com.bank.dto.AccountResponse;
import com.bank.dto.ErrorResponse;
import com.bank.dto.MonetaryOperationResponse;
import com.bank.dto.MonetaryRequest;
import com.bank.dto.OperationResult;
import com.bank.dto.TransactionResponse;
import com.bank.dto.TransferRequest;
import com.bank.dto.TransferResponse;
import com.bank.exception.UnauthorizedException;
import com.bank.model.Account;
import com.bank.model.AccountStatus;
import com.bank.model.IdempotencyRecord;
import com.bank.model.Transaction;
import com.bank.model.TransactionDirection;
import com.bank.model.TransactionStatus;
import com.bank.repository.AccountRepository;
import com.bank.repository.IdempotencyRecordRepository;
import com.bank.repository.TransactionRepository;
import com.bank.security.AuthenticatedUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonetaryOperationService {

    private static final int DESCRIPTION_MAX_LENGTH = 255;
    private static final String DEPOSIT = "DEPOSIT";
    private static final String WITHDRAW = "WITHDRAW";
    private static final String TRANSFER = "TRANSFER";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final AuthorizationService authorizationService;
    private final ObjectMapper objectMapper;

    public MonetaryOperationService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            AuthorizationService authorizationService,
            ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.authorizationService = authorizationService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OperationResult deposit(Long accountId, MonetaryRequest request, String idempotencyKey, AuthenticatedUser user) {
        OperationResult keyError = validateIdempotencyKey(idempotencyKey);
        if (keyError != null) {
            return keyError;
        }
        String storageKey = storageKey(user, idempotencyKey);
        OperationResult replay = loadReplay(storageKey);
        if (replay != null) {
            return replay;
        }

        OperationResult result = validateAccountPath(accountId);
        if (result == null) {
            Account account = loadActiveAccount(accountId);
            if (account == null) {
                result = notFound("ACCOUNT_NOT_FOUND", "Account not found");
            } else {
                result = authorizeAccount(user, account);
                if (result == null) {
                    BigDecimal amount = validateAmount(request);
                    if (amount == null) {
                        result = unprocessable("INVALID_AMOUNT", "Amount must be greater than 0 with at most 2 decimal places", "amount");
                    } else {
                        result = validateDescription(request == null ? null : request.description());
                        if (result == null) {
                            account.setBalance(account.getBalance().add(amount));
                            Transaction transaction = createTransaction(
                                    account,
                                    amount,
                                    TransactionDirection.CREDIT,
                                    TransactionStatus.SUCCESS,
                                    request.description(),
                                    "External deposit",
                                    null,
                                    idempotencyKey);
                            accountRepository.save(account);
                            transactionRepository.save(transaction);
                            result = ok(new MonetaryOperationResponse(
                                    "Deposit completed successfully",
                                    AccountResponse.from(account),
                                    TransactionResponse.from(transaction)));
                        }
                    }
                }
            }
        }

        return persistAndReturn(storageKey, idempotencyKey, user, DEPOSIT, result);
    }

    @Transactional
    public OperationResult withdraw(Long accountId, MonetaryRequest request, String idempotencyKey, AuthenticatedUser user) {
        OperationResult keyError = validateIdempotencyKey(idempotencyKey);
        if (keyError != null) {
            return keyError;
        }
        String storageKey = storageKey(user, idempotencyKey);
        OperationResult replay = loadReplay(storageKey);
        if (replay != null) {
            return replay;
        }

        OperationResult result = validateAccountPath(accountId);
        if (result == null) {
            Account account = loadActiveAccount(accountId);
            if (account == null) {
                result = notFound("ACCOUNT_NOT_FOUND", "Account not found");
            } else {
                result = authorizeAccount(user, account);
                if (result == null) {
                    BigDecimal amount = validateAmount(request);
                    if (amount == null) {
                        result = unprocessable("INVALID_AMOUNT", "Amount must be greater than 0 with at most 2 decimal places", "amount");
                    } else {
                        result = validateDescription(request == null ? null : request.description());
                        if (result == null && account.getBalance().compareTo(amount) < 0) {
                            Transaction failedTransaction = createTransaction(
                                    account,
                                    amount,
                                    TransactionDirection.DEBIT,
                                    TransactionStatus.FAILED,
                                    request.description(),
                                    null,
                                    "Cash withdrawal",
                                    idempotencyKey);
                            transactionRepository.save(failedTransaction);
                            result = conflict("INSUFFICIENT_FUNDS", "Withdrawal would make balance negative", null);
                        }
                        if (result == null) {
                            account.setBalance(account.getBalance().subtract(amount));
                            Transaction transaction = createTransaction(
                                    account,
                                    amount,
                                    TransactionDirection.DEBIT,
                                    TransactionStatus.SUCCESS,
                                    request.description(),
                                    null,
                                    "Cash withdrawal",
                                    idempotencyKey);
                            accountRepository.save(account);
                            transactionRepository.save(transaction);
                            result = ok(new MonetaryOperationResponse(
                                    "Withdrawal completed successfully",
                                    AccountResponse.from(account),
                                    TransactionResponse.from(transaction)));
                        }
                    }
                }
            }
        }

        return persistAndReturn(storageKey, idempotencyKey, user, WITHDRAW, result);
    }

    @Transactional
    public OperationResult transfer(TransferRequest request, String idempotencyKey, AuthenticatedUser user) {
        OperationResult keyError = validateIdempotencyKey(idempotencyKey);
        if (keyError != null) {
            return keyError;
        }
        String storageKey = storageKey(user, idempotencyKey);
        OperationResult replay = loadReplay(storageKey);
        if (replay != null) {
            return replay;
        }

        OperationResult result = validateTransferRequest(request);
        if (result == null) {
            Account fromAccount = loadActiveAccount(request.fromAccountId());
            if (fromAccount == null) {
                result = notFound("ACCOUNT_NOT_FOUND", "Source account not found", null);
            } else {
                result = authorizeAccount(user, fromAccount);
                if (result == null) {
                    Account toAccount = loadActiveAccount(request.toAccountId());
                    if (toAccount == null) {
                        result = notFound("ACCOUNT_NOT_FOUND", "Destination account not found", null);
                    } else {
                        BigDecimal amount = request.amount().setScale(2, RoundingMode.UNNECESSARY);
                        if (fromAccount.getBalance().compareTo(amount) < 0) {
                            Transaction failedDebit = createTransaction(
                                    fromAccount,
                                    amount,
                                    TransactionDirection.DEBIT,
                                    TransactionStatus.FAILED,
                                    request.description(),
                                    null,
                                    "Account " + toAccount.getAccountId(),
                                    idempotencyKey);
                            Transaction failedCredit = createTransaction(
                                    toAccount,
                                    amount,
                                    TransactionDirection.CREDIT,
                                    TransactionStatus.FAILED,
                                    request.description(),
                                    "Account " + fromAccount.getAccountId(),
                                    null,
                                    idempotencyKey);
                            transactionRepository.save(failedDebit);
                            transactionRepository.save(failedCredit);
                            result = conflict("INSUFFICIENT_FUNDS", "Transfer would make balance negative", null);
                        }
                        if (result == null) {
                            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
                            toAccount.setBalance(toAccount.getBalance().add(amount));

                            Transaction debitTransaction = createTransaction(
                                    fromAccount,
                                    amount,
                                    TransactionDirection.DEBIT,
                                    TransactionStatus.SUCCESS,
                                    request.description(),
                                    null,
                                    "Account " + toAccount.getAccountId(),
                                    idempotencyKey);
                            Transaction creditTransaction = createTransaction(
                                    toAccount,
                                    amount,
                                    TransactionDirection.CREDIT,
                                    TransactionStatus.SUCCESS,
                                    request.description(),
                                    "Account " + fromAccount.getAccountId(),
                                    null,
                                    idempotencyKey);

                            accountRepository.save(fromAccount);
                            accountRepository.save(toAccount);
                            transactionRepository.save(debitTransaction);
                            transactionRepository.save(creditTransaction);

                            result = ok(new TransferResponse(
                                    "Transfer completed successfully",
                                    AccountResponse.from(fromAccount),
                                    AccountResponse.from(toAccount),
                                    TransactionResponse.from(debitTransaction),
                                    TransactionResponse.from(creditTransaction)));
                        }
                    }
                }
            }
        }

        return persistAndReturn(storageKey, idempotencyKey, user, TRANSFER, result);
    }

    private OperationResult loadReplay(String storageKey) {
        return idempotencyRecordRepository.findById(storageKey)
                .map(record -> new OperationResult(httpStatus(record.getResponseStatus()), parseBody(record.getResponseBody())))
                .orElse(null);
    }

    private OperationResult persistAndReturn(
            String storageKey,
            String idempotencyKey,
            AuthenticatedUser user,
            String operationType,
            OperationResult result) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.setStorageKey(storageKey);
        record.setIdempotencyKey(idempotencyKey);
        record.setCallerUserId(user.userId());
        record.setOperationType(operationType);
        record.setResponseStatus(result.status().value());
        record.setResponseBody(writeBody(result.body()));
        idempotencyRecordRepository.save(record);
        return result;
    }

    private Account loadActiveAccount(Long accountId) {
        return accountRepository.findByAccountIdAndDeletedAtIsNull(accountId)
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE)
                .orElse(null);
    }

    private OperationResult authorizeAccount(AuthenticatedUser user, Account account) {
        try {
            authorizationService.assertCanAccessAccount(user, account);
            return null;
        } catch (UnauthorizedException ex) {
            return unauthorized("UNAUTHORIZED", "Unauthorized", null);
        }
    }

    private OperationResult validateAccountPath(Long accountId) {
        if (accountId == null || accountId <= 0) {
            return badRequest("INVALID_ACCOUNT_ID", "Account ID must be greater than 0", "accountId");
        }
        return null;
    }

    private OperationResult validateTransferRequest(TransferRequest request) {
        if (request == null) {
            return badRequest("INVALID_REQUEST", "Request body is required", null);
        }
        if (request.fromAccountId() == null || request.fromAccountId() <= 0) {
            return badRequest("INVALID_ACCOUNT_ID", "Source account ID must be greater than 0", "fromAccountId");
        }
        if (request.toAccountId() == null || request.toAccountId() <= 0) {
            return badRequest("INVALID_ACCOUNT_ID", "Destination account ID must be greater than 0", "toAccountId");
        }
        if (request.fromAccountId().equals(request.toAccountId())) {
            return unprocessable("INVALID_TRANSFER_ACCOUNT", "Source and destination accounts must be different", "toAccountId");
        }
        BigDecimal amount = validateAmount(new MonetaryRequest(request.amount(), request.description()));
        if (amount == null) {
            return unprocessable("INVALID_AMOUNT", "Amount must be greater than 0 with at most 2 decimal places", "amount");
        }
        return validateDescription(request.description());
    }

    private BigDecimal validateAmount(MonetaryRequest request) {
        if (request == null || request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (request.amount().scale() > 2) {
            return null;
        }
        return request.amount().setScale(2, RoundingMode.UNNECESSARY);
    }

    private OperationResult validateDescription(String description) {
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            return unprocessable("INVALID_DESCRIPTION", "Description must be 255 characters or fewer", "description");
        }
        return null;
    }

    private OperationResult validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return badRequest("INVALID_IDEMPOTENCY_KEY", "Idempotency-Key header is required", "Idempotency-Key");
        }
        return null;
    }

    private String storageKey(AuthenticatedUser user, String idempotencyKey) {
        String callerUserId = user != null && user.userId() != null ? user.userId() : "anonymous";
        return callerUserId + ":" + idempotencyKey;
    }

    private Transaction createTransaction(
            Account account,
            BigDecimal amount,
            TransactionDirection direction,
            TransactionStatus status,
            String description,
            String senderInfo,
            String receiverInfo,
            String idempotencyKey) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setDirection(direction);
        transaction.setStatus(status);
        transaction.setDescription(description);
        transaction.setSenderInfo(senderInfo);
        transaction.setReceiverInfo(receiverInfo);
        transaction.setIdempotencyKey(idempotencyKey);
        return transaction;
    }

    private JsonNode parseBody(String responseBody) {
        try {
            return objectMapper.readTree(responseBody);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize idempotency response", ex);
        }
    }

    private String writeBody(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize idempotency response", ex);
        }
    }

    private HttpStatus httpStatus(int statusCode) {
        return HttpStatus.valueOf(statusCode);
    }

    private OperationResult ok(Object body) {
        return new OperationResult(HttpStatus.OK, body);
    }

    private OperationResult badRequest(String code, String message, String field) {
        return new OperationResult(HttpStatus.BAD_REQUEST, new ErrorResponse(code, message, field));
    }

    private OperationResult unauthorized(String code, String message, String field) {
        return new OperationResult(HttpStatus.UNAUTHORIZED, new ErrorResponse(code, message, field));
    }

    private OperationResult notFound(String code, String message) {
        return new OperationResult(HttpStatus.NOT_FOUND, new ErrorResponse(code, message, null));
    }

    private OperationResult notFound(String code, String message, String field) {
        return new OperationResult(HttpStatus.NOT_FOUND, new ErrorResponse(code, message, field));
    }

    private OperationResult conflict(String code, String message, String field) {
        return new OperationResult(HttpStatus.CONFLICT, new ErrorResponse(code, message, field));
    }

    private OperationResult unprocessable(String code, String message, String field) {
        return new OperationResult(HttpStatus.UNPROCESSABLE_ENTITY, new ErrorResponse(code, message, field));
    }
}
