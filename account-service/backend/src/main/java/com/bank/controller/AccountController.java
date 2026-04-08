package com.bank.controller;

import com.bank.dto.AccountResponse;
import com.bank.dto.CreateAccountRequest;
import com.bank.dto.MonetaryRequest;
import com.bank.dto.OperationResult;
import com.bank.dto.TransferRequest;
import com.bank.dto.UpdateAccountRequest;
import com.bank.security.AuthenticatedUser;
import com.bank.service.AccountService;
import com.bank.service.MonetaryOperationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return accountService.createAccount(customerId, request, user);
    }

    @GetMapping("/accounts/{accountId}")
    public AccountResponse getAccount(@PathVariable Long accountId, @AuthenticationPrincipal AuthenticatedUser user) {
        return accountService.getAccount(accountId, user);
    }

    @GetMapping("/customers/{customerId}/accounts")
    public List<AccountResponse> listCustomerAccounts(
            @PathVariable Long customerId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return accountService.listCustomerAccounts(customerId, user);
    }

    @PutMapping("/accounts/{accountId}")
    public AccountResponse updateAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody UpdateAccountRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return accountService.updateAccount(accountId, request, user);
    }

    @DeleteMapping("/accounts/{accountId}")
    public Map<String, String> deleteAccount(
            @PathVariable Long accountId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        accountService.deleteAccount(accountId, user);
        return Map.of("message", "Account deleted successfully");
    }

    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<Object> deposit(
            @PathVariable Long accountId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody(required = false) MonetaryRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        OperationResult result = monetaryOperationService.deposit(accountId, request, idempotencyKey, user);
        return ResponseEntity.status(result.status()).body(result.body());
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public ResponseEntity<Object> withdraw(
            @PathVariable Long accountId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody(required = false) MonetaryRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        OperationResult result = monetaryOperationService.withdraw(accountId, request, idempotencyKey, user);
        return ResponseEntity.status(result.status()).body(result.body());
    }

    @PostMapping("/accounts/transfer")
    public ResponseEntity<Object> transfer(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody(required = false) TransferRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        OperationResult result = monetaryOperationService.transfer(request, idempotencyKey, user);
        return ResponseEntity.status(result.status()).body(result.body());
    }
}
