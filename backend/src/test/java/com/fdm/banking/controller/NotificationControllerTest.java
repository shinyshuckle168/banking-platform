package com.fdm.banking.controller;

import com.fdm.banking.dto.response.NotificationDecisionResponse;
import com.fdm.banking.exception.GlobalExceptionHandler;
import com.fdm.banking.service.NotificationEvaluationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller slice tests for NotificationController. (T077)
 */
@WebMvcTest(controllers = NotificationController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationEvaluationService notificationEvaluationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void evaluate_returnsOk() throws Exception {
        NotificationDecisionResponse resp = new NotificationDecisionResponse();
        resp.setDecision("RAISED");
        when(notificationEvaluationService.evaluate(any())).thenReturn(resp);

        String body = """
                {
                  "eventId": "evt001",
                  "eventType": "StandingOrderFailure",
                  "accountId": 1,
                  "customerId": 42,
                  "businessTimestamp": "2026-01-01T10:00:00"
                }
                """;

        mockMvc.perform(post("/notifications/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )
                .andExpect(status().isOk());
    }
}
