package com.fdm.banking.controller;

import com.fdm.banking.exception.GlobalExceptionHandler;
import com.fdm.banking.security.JwtAuthenticationToken;
import com.fdm.banking.security.UserPrincipal;
import com.fdm.banking.service.MonthlyStatementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for StatementController. (T085)
 */
@WebMvcTest(controllers = StatementController.class)
@Import(GlobalExceptionHandler.class)
class StatementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MonthlyStatementService monthlyStatementService;

    private JwtAuthenticationToken buildAuth() {
        UserPrincipal principal = new UserPrincipal(1L, "user", "CUSTOMER",
                List.of("STATEMENT:READ"), 10L);
        return new JwtAuthenticationToken(principal);
    }

    @Test
    void getStatement_returnsOkWithPdfContentType() throws Exception {
        byte[] fakePdf = "%PDF-1.4 fake".getBytes();
        when(monthlyStatementService.generateStatement(anyLong(), anyString(), any()))
                .thenReturn(fakePdf);

        mockMvc.perform(get("/accounts/1/statements/2024-01")
                        .with(authentication(buildAuth())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("statement-1-2024-01.pdf")));
    }
}
