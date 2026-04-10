package com.fdm.banking.controller;

import com.fdm.banking.dto.response.StandingOrderListResponse;
import com.fdm.banking.exception.GlobalExceptionHandler;
import com.fdm.banking.exception.PermissionDeniedException;
import com.fdm.banking.security.JwtAuthenticationToken;
import com.fdm.banking.security.UserPrincipal;
import com.fdm.banking.service.StandingOrderService;
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
 * Controller slice tests for StandingOrderController. (T058)
 */
@WebMvcTest(controllers = StandingOrderController.class)
@Import(GlobalExceptionHandler.class)
class StandingOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StandingOrderService standingOrderService;

    private JwtAuthenticationToken buildAuth() {
        UserPrincipal principal = new UserPrincipal(1L, "user", "CUSTOMER",
                List.of("STANDING_ORDER:READ"), 10L);
        return new JwtAuthenticationToken(principal);
    }

    @Test
    void listOrders_returnsOk() throws Exception {
        StandingOrderListResponse resp = new StandingOrderListResponse();
        resp.setStandingOrders(List.of());
        when(standingOrderService.list(anyLong(), any())).thenReturn(resp);

        mockMvc.perform(get("/accounts/1/standing-orders")
                        .with(authentication(buildAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void listOrders_permissionDenied_returns401() throws Exception {
        when(standingOrderService.list(anyLong(), any()))
                .thenThrow(new PermissionDeniedException("STANDING_ORDER:READ"));

        mockMvc.perform(get("/accounts/1/standing-orders")
                        .with(authentication(buildAuth())))
                .andExpect(status().isUnauthorized());
    }
}
