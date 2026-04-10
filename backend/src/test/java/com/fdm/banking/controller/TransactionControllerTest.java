package com.fdm.banking.controller;

import com.fdm.banking.dto.response.TransactionHistoryResponse;
import com.fdm.banking.exception.GlobalExceptionHandler;
import com.fdm.banking.exception.PermissionDeniedException;
import com.fdm.banking.security.JwtAuthenticationToken;
import com.fdm.banking.security.UserPrincipal;
import com.fdm.banking.service.TransactionHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for TransactionController. (T032)
 */
@WebMvcTest(controllers = TransactionController.class)
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionHistoryService transactionHistoryService;

    private JwtAuthenticationToken buildAuth() {
        UserPrincipal principal = new UserPrincipal(1L, "user", "CUSTOMER", List.of("TX:VIEW"), 10L);
        return new JwtAuthenticationToken(principal);
    }

    @Test
    void getHistory_returnsOk() throws Exception {
        TransactionHistoryResponse response = new TransactionHistoryResponse();
        response.setAccountId(1L);
        when(transactionHistoryService.getHistory(anyLong(), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/accounts/1/transactions")
                        .with(authentication(buildAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void permissionDenied_returns401() throws Exception {
        when(transactionHistoryService.getHistory(anyLong(), any(), any(), any()))
                .thenThrow(new PermissionDeniedException("TX:VIEW"));

        mockMvc.perform(get("/accounts/1/transactions")
                        .with(authentication(buildAuth())))
                .andExpect(status().isUnauthorized());
    }
}
