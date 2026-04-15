// package com.group1.banking.controller;

// import com.group1.banking.dto.response.TransactionHistoryResponse;
// import com.group1.banking.security.JwtAuthenticationFilter;
// import com.group1.banking.security.UserPrincipal;
// import com.group1.banking.service.TransactionHistoryService;
// import org.springframework.format.annotation.DateTimeFormat;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.*;

// import java.time.LocalDate;

// /**
//  * Transaction history controller. (T029)
//  */
// @RestController
// @RequestMapping("/accounts/{accountId}/transactions")
// public class TransactionController {

//     private final TransactionHistoryService transactionHistoryService;

//     public TransactionController(TransactionHistoryService transactionHistoryService) {
//         this.transactionHistoryService = transactionHistoryService;
//     }

//     @GetMapping
//     public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(
//             @PathVariable long accountId,
//             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
//             Authentication authentication) {
//         UserPrincipal caller = extractPrincipal(authentication);
//         TransactionHistoryResponse response = transactionHistoryService.getHistory(
//                 accountId, startDate, endDate, caller);
//         return ResponseEntity.ok(response);
//     }

//     @GetMapping("/export")
//     public ResponseEntity<byte[]> exportTransactionsAsPdf(
//             @PathVariable long accountId,
//             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
//             Authentication authentication) {
//         UserPrincipal caller = extractPrincipal(authentication);
//         byte[] pdfBytes = transactionHistoryService.exportPdf(accountId, startDate, endDate, caller);

//         String filename = "statement-" + accountId + "-"
//                 + (startDate != null ? startDate : "default") + "-"
//                 + (endDate != null ? endDate : "default") + ".pdf";

//         return ResponseEntity.ok()
//                 .contentType(MediaType.APPLICATION_PDF)
//                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
//                 .body(pdfBytes);
//     }

//     private UserPrincipal extractPrincipal(Authentication authentication) {
//         if (authentication instanceof JwtAuthenticationFilter jwtToken) {
//             return jwtToken.getPrincipal();
//         }
//         throw new com.group1.banking.exception.PermissionDeniedException("JWT");
//     }

//     @GetMapping("/test")
//     public ResponseEntity<String> test(Authentication authentication) {
//     if (authentication == null) {
//         return ResponseEntity.status(403).body("No authentication found");
//     }
//     return ResponseEntity.ok("Auth type: " + authentication.getClass().getSimpleName() 
//         + " | Principal: " + authentication.getPrincipal());
//     }
// }
