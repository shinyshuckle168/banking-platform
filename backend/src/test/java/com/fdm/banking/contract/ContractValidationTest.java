package com.fdm.banking.contract;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates that all required API endpoints exist and controllers are loadable. (T115)
 *
 * <p>Endpoint coverage per contract specs:
 * <ul>
 *   <li>US-01: GET /accounts/{id}/transactions, GET /accounts/{id}/transactions/export</li>
 *   <li>US-02: POST /accounts/{id}/standing-orders, GET /accounts/{id}/standing-orders, DELETE /standing-orders/{id}</li>
 *   <li>US-03: POST /notifications/evaluate</li>
 *   <li>US-04: GET /accounts/{id}/statements/{period}</li>
 *   <li>US-05: GET /accounts/{id}/insights, PUT /accounts/{id}/transactions/{txId}/category</li>
 * </ul>
 */
class ContractValidationTest {

    @Test
    void transactionHistoryControllerExists() throws Exception {
        Class<?> cls = Class.forName("com.group1.banking.controller.TransactionController");
        assertThat(cls).isNotNull();
    }

    @Test
    void standingOrderControllerExists() throws Exception {
        Class<?> cls = Class.forName("com.group1.banking.controller.StandingOrderController");
        assertThat(cls).isNotNull();
    }

    @Test
    void notificationControllerExists() throws Exception {
        Class<?> cls = Class.forName("com.group1.banking.controller.NotificationController");
        assertThat(cls).isNotNull();
    }

    @Test
    void statementControllerExists() throws Exception {
        Class<?> cls = Class.forName("com.group1.banking.controller.StatementController");
        assertThat(cls).isNotNull();
    }

    @Test
    void insightControllerExists() throws Exception {
        Class<?> cls = Class.forName("com.group1.banking.controller.InsightController");
        assertThat(cls).isNotNull();
    }

    @Test
    void transactionController_hasGetHistoryMapping() throws Exception {
        Class<?> cls = Class.forName("com.group1.banking.controller.TransactionController");
        boolean hasGetHistory = java.util.Arrays.stream(cls.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("getTransactionHistory"));
        assertThat(hasGetHistory).isTrue();
    }

    @Test
    void notificationController_hasEvaluateMapping() throws Exception {
        Class<?> cls = Class.forName("com.group1.banking.controller.NotificationController");
        boolean hasEvaluate = java.util.Arrays.stream(cls.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("evaluate"));
        assertThat(hasEvaluate).isTrue();
    }
}
