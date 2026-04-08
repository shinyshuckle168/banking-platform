package com.bankapp.customer.contract;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CreateCustomerContractTest {

    private static final Path OPENAPI_PATH = Path.of("..", "specs", "001-bank-auth-customer", "contracts", "openapi.yaml");

    @Test
    void createCustomerContractDefinesExpectedRouteAndResponses() throws IOException {
        String contract = Files.readString(OPENAPI_PATH);

        assertAll(
                () -> assertTrue(contract.contains("/api/customers:")),
                () -> assertTrue(contract.contains("operationId: createCustomer")),
                () -> assertTrue(contract.contains("bearerAuth")),
                () -> assertTrue(contract.contains("#/components/schemas/CreateCustomerRequest")),
                () -> assertTrue(contract.contains("#/components/schemas/CustomerResponse")),
                () -> assertTrue(contract.contains("INVALID_CUSTOMER_TYPE"))
        );
    }
}
