package com.fdm.banking.controller;

import com.fdm.banking.exception.PermissionDeniedException;
import com.fdm.banking.security.JwtAuthenticationToken;
import com.fdm.banking.security.UserPrincipal;
import com.fdm.banking.service.MonthlyStatementService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    public ResponseEntity<byte[]> getStatement(
            @PathVariable long accountId,
            @PathVariable String period,
            Authentication authentication) {
        UserPrincipal caller = extractPrincipal(authentication);
        byte[] pdfBytes = monthlyStatementService.generateStatement(accountId, period, caller);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
                "attachment",
                "statement-" + accountId + "-" + period + ".pdf");
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    private UserPrincipal extractPrincipal(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getPrincipal();
        }
        throw new PermissionDeniedException("JWT");
    }
}
