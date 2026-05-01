package com.group1.banking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.group1.banking.dto.customer.AccountResponse;
import com.group1.banking.dto.customer.CreateAccountRequest;
import com.group1.banking.dto.customer.MonetaryRequest;
import com.group1.banking.dto.customer.OperationResult;
import com.group1.banking.dto.customer.TransferRequest;
import com.group1.banking.dto.customer.UpdateAccountRequest;
import com.group1.banking.service.impl.AccountService;
import com.group1.banking.service.impl.MonetaryOperationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping
public class AccountController {

    private final AccountService accountService;
    private final MonetaryOperationService monetaryOperationService;

    public AccountController(AccountService accountService, MonetaryOperationService monetaryOperationService) {
        this.accountService = accountService;
        this.monetaryOperationService = monetaryOperationService;
    }

    @PostMapping("/customers/{customerId}/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(
            @PathVariable Long customerId,
            @Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(customerId, request);
    }

    @GetMapping("/accounts/{accountId}")
    public AccountResponse getAccount(@PathVariable Long accountId) {
        return accountService.getAccount(accountId);
    }

    @GetMapping("/customers/{customerId}/accounts")
    public List<AccountResponse> listCustomerAccounts(@PathVariable Long customerId) {
        return accountService.listCustomerAccounts(customerId);
    }

    @PutMapping("/accounts/{accountId}")
    public AccountResponse updateAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody UpdateAccountRequest request) {
        return accountService.updateAccount(accountId, request);
    }

    @DeleteMapping("/accounts/{accountId}")
    public Map<String, String> deleteAccount(@PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
        return Map.of("message", "Account deleted successfully");
    }

    @PostMapping("/accounts/{accountId}/close")
    public Map<String, Object> closeRrspAccount(@PathVariable Long accountId) {
        return accountService.closeRrspAccount(accountId);
    }

    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<Object> deposit(
            @PathVariable Long accountId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody(required = false) MonetaryRequest request) {
        OperationResult result = monetaryOperationService.deposit(accountId, request, idempotencyKey);
        return ResponseEntity.status(result.status()).body(result.body());
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public ResponseEntity<Object> withdraw(
            @PathVariable Long accountId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody(required = false) MonetaryRequest request) {
        OperationResult result = monetaryOperationService.withdraw(accountId, request, idempotencyKey);
        return ResponseEntity.status(result.status()).body(result.body());
    }

    @PostMapping("/accounts/transfer")
    public ResponseEntity<Object> transfer(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody(required = false) TransferRequest request) {
        OperationResult result = monetaryOperationService.transfer(request, idempotencyKey);
        return ResponseEntity.status(result.status()).body(result.body());
    }
}