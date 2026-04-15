//package com.fdm.banking.controller;
//
//import com.fdm.banking.dto.response.SpendingInsightResponse;
//import com.fdm.banking.exception.GlobalExceptionHandler;
//import com.fdm.banking.security.JwtAuthenticationToken;
//import com.fdm.banking.security.UserPrincipal;
//import com.fdm.banking.service.SpendingInsightService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.when;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
///**
// * Controller slice tests for InsightController. (T102)
// */
//@WebMvcTest(controllers = InsightController.class)
//@Import(GlobalExceptionHandler.class)
//class InsightControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private SpendingInsightService spendingInsightService;
//
//    private JwtAuthenticationToken buildAuth() {
//        UserPrincipal principal = new UserPrincipal("1", "user", List.of("CUSTOMER"),
//                List.of("INSIGHTS:READ"), 10L);
//        return new JwtAuthenticationToken(principal);
//    }
//
//    @Test
//    void getInsights_returnsOk() throws Exception {
//        SpendingInsightResponse resp = new SpendingInsightResponse();
//        resp.setAccountId(1L);
//        when(spendingInsightService.getInsights(anyLong(), anyInt(), anyInt(), any()))
//                .thenReturn(resp);
//
//        mockMvc.perform(get("/accounts/1/insights?year=2024&month=1")
//                        .with(authentication(buildAuth())))
//                .andExpect(status().isOk());
//    }
//}
