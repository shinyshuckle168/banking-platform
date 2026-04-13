package com.fdm.banking.controller;

import com.fdm.banking.dto.request.RecategoriseRequest;
import com.fdm.banking.dto.response.RecategoriseResponse;
import com.fdm.banking.dto.response.SpendingInsightResponse;
import com.fdm.banking.exception.PermissionDeniedException;
import com.fdm.banking.security.JwtAuthenticationToken;
import com.fdm.banking.security.UserPrincipal;
import com.fdm.banking.service.SpendingInsightService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Spending insight controller. (T099)
 */
@RestController
@RequestMapping("/accounts/{accountId}")
public class InsightController {

    private final SpendingInsightService spendingInsightService;

    public InsightController(SpendingInsightService spendingInsightService) {
        this.spendingInsightService = spendingInsightService;
    }

    @GetMapping("/insights")
    public ResponseEntity<SpendingInsightResponse> getInsights(
            @PathVariable long accountId,
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        UserPrincipal caller = extractPrincipal(authentication);
        return ResponseEntity.ok(spendingInsightService.getInsights(accountId, year, month, caller));
    }

    @PutMapping("/transactions/{transactionId}/category")
    public ResponseEntity<RecategoriseResponse> recategorise(
            @PathVariable long accountId,
            @PathVariable long transactionId,
            @RequestBody @Valid RecategoriseRequest request,
            Authentication authentication) {
        UserPrincipal caller = extractPrincipal(authentication);
        return ResponseEntity.ok(spendingInsightService.recategorise(
                accountId, transactionId, request.getCategory(), caller));
    }

    private UserPrincipal extractPrincipal(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getPrincipal();
        }
        throw new PermissionDeniedException("JWT");
    }
}
