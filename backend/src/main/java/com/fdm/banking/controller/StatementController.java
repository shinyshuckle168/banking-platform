package com.fdm.banking.controller;

import com.fdm.banking.dto.response.MonthlyStatementResponse;
import com.fdm.banking.exception.PermissionDeniedException;
import com.fdm.banking.security.JwtAuthenticationToken;
import com.fdm.banking.security.UserPrincipal;
import com.fdm.banking.service.MonthlyStatementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Statement controller. (T083)
 */
@RestController
@RequestMapping("/accounts/{accountId}/statements")
public class StatementController {

    private final MonthlyStatementService monthlyStatementService;

    public StatementController(MonthlyStatementService monthlyStatementService) {
        this.monthlyStatementService = monthlyStatementService;
    }

    @GetMapping("/{period}")
    public ResponseEntity<MonthlyStatementResponse> getStatement(
            @PathVariable long accountId,
            @PathVariable String period,
            @RequestParam(required = false) Integer version,
            Authentication authentication) {
        UserPrincipal caller = extractPrincipal(authentication);
        return ResponseEntity.ok(monthlyStatementService.getStatement(accountId, period, version, caller));
    }

    private UserPrincipal extractPrincipal(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getPrincipal();
        }
        throw new PermissionDeniedException("JWT");
    }
}
