package com.bankapp.customer.contract;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class UpdateCustomerContractTest {

    private static final Path OPENAPI_PATH = Path.of("..", "specs", "001-bank-auth-customer", "contracts", "openapi.yaml");

    @Test
    void updateCustomerContractIncludesImmutableAndConflictResponses() throws IOException {
        String contract = Files.readString(OPENAPI_PATH);

        assertAll(
                () -> assertTrue(contract.contains("operationId: updateCustomer")),
                () -> assertTrue(contract.contains("FIELD_NOT_UPDATABLE")),
                () -> assertTrue(contract.contains("CUSTOMER_CONFLICT")),
                () -> assertTrue(contract.contains("accountNumber")),
                () -> assertTrue(contract.contains("updatedAt:"))
        );
    }
}
