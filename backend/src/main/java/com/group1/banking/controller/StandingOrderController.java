// package com.group1.banking.controller;

// import com.group1.banking.dto.request.CreateStandingOrderRequest;
// import com.group1.banking.dto.response.CancelStandingOrderResponse;
// import com.group1.banking.dto.response.StandingOrderListResponse;
// import com.group1.banking.dto.response.StandingOrderResponse;
// import com.group1.banking.exception.PermissionDeniedException;
// import com.group1.banking.security.JwtAuthenticationFilter;
// import com.group1.banking.security.UserPrincipal;
// import com.group1.banking.service.StandingOrderService;
// import jakarta.validation.Valid;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.*;

// /**
//  * Standing order controller. (T053)
//  */
// @RestController
// public class StandingOrderController {

//     private final StandingOrderService standingOrderService;

//     public StandingOrderController(StandingOrderService standingOrderService) {
//         this.standingOrderService = standingOrderService;
//     }

//     @PostMapping("/accounts/{accountId}/standing-orders")
//     public ResponseEntity<StandingOrderResponse> createStandingOrder(
//             @PathVariable long accountId,
//             @RequestBody @Valid CreateStandingOrderRequest request,
//             Authentication authentication) {
//         UserPrincipal caller = extractPrincipal(authentication);
//         StandingOrderResponse response = standingOrderService.create(accountId, request, caller);
//         return ResponseEntity.status(HttpStatus.CREATED).body(response);
//     }

//     @GetMapping("/accounts/{accountId}/standing-orders")
//     public ResponseEntity<StandingOrderListResponse> listStandingOrders(
//             @PathVariable long accountId,
//             Authentication authentication) {
//         UserPrincipal caller = extractPrincipal(authentication);
//         return ResponseEntity.ok(standingOrderService.list(accountId, caller));
//     }

//     @DeleteMapping("/standing-orders/{standingOrderId}")
//     public ResponseEntity<CancelStandingOrderResponse> cancelStandingOrder(
//             @PathVariable String standingOrderId,
//             Authentication authentication) {
//         UserPrincipal caller = extractPrincipal(authentication);
//         return ResponseEntity.ok(standingOrderService.cancel(standingOrderId, caller));
//     }

//     private UserPrincipal extractPrincipal(Authentication authentication) {
//         if (authentication instanceof JwtAuthenticationFilter jwtToken) {
//             return jwtToken.getPrincipal();
//         }
//         throw new PermissionDeniedException("JWT");
//     }
// }
