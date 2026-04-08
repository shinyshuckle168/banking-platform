package com.bankapp.contract;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class OpenApiComplianceTest {

    private static final Path OPENAPI_PATH = Path.of("..", "specs", "001-bank-auth-customer", "contracts", "openapi.yaml");

    @Test
    void openApiDefinesAllImplementedEndpoints() throws IOException {
        String contract = Files.readString(OPENAPI_PATH);

        assertAll(
                () -> assertTrue(contract.contains("/api/auth/register:")),
                () -> assertTrue(contract.contains("/api/auth/login:")),
                () -> assertTrue(contract.contains("/api/customers:")),
                () -> assertTrue(contract.contains("operationId: updateCustomer")),
                () -> assertTrue(contract.contains("operationId: getCustomer")),
                () -> assertTrue(contract.contains("ErrorResponse:"))
        );
    }
}
