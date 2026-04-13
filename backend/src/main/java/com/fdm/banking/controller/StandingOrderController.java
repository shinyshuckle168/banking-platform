package com.fdm.banking.controller;

import com.fdm.banking.dto.request.CreateStandingOrderRequest;
import com.fdm.banking.dto.response.CancelStandingOrderResponse;
import com.fdm.banking.dto.response.StandingOrderListResponse;
import com.fdm.banking.dto.response.StandingOrderResponse;
import com.fdm.banking.exception.PermissionDeniedException;
import com.fdm.banking.security.JwtAuthenticationToken;
import com.fdm.banking.security.UserPrincipal;
import com.fdm.banking.service.StandingOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Standing order controller. (T053)
 */
@RestController
public class StandingOrderController {

    private final StandingOrderService standingOrderService;

    public StandingOrderController(StandingOrderService standingOrderService) {
        this.standingOrderService = standingOrderService;
    }

    @PostMapping("/accounts/{accountId}/standing-orders")
    public ResponseEntity<StandingOrderResponse> createStandingOrder(
            @PathVariable long accountId,
            @RequestBody @Valid CreateStandingOrderRequest request,
            Authentication authentication) {
        UserPrincipal caller = extractPrincipal(authentication);
        StandingOrderResponse response = standingOrderService.create(accountId, request, caller);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/accounts/{accountId}/standing-orders")
    public ResponseEntity<StandingOrderListResponse> listStandingOrders(
            @PathVariable long accountId,
            Authentication authentication) {
        UserPrincipal caller = extractPrincipal(authentication);
        return ResponseEntity.ok(standingOrderService.list(accountId, caller));
    }

    @DeleteMapping("/standing-orders/{standingOrderId}")
    public ResponseEntity<CancelStandingOrderResponse> cancelStandingOrder(
            @PathVariable String standingOrderId,
            Authentication authentication) {
        UserPrincipal caller = extractPrincipal(authentication);
        return ResponseEntity.ok(standingOrderService.cancel(standingOrderId, caller));
    }

    private UserPrincipal extractPrincipal(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getPrincipal();
        }
        throw new PermissionDeniedException("JWT");
    }
}
