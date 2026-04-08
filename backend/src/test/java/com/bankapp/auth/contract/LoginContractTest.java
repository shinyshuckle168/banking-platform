package com.bankapp.auth.contract;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class LoginContractTest {

    private static final Path OPENAPI_PATH = Path.of("..", "specs", "001-bank-auth-customer", "contracts", "openapi.yaml");

    @Test
    void loginContractDefinesExpectedRouteAndResponses() throws IOException {
        String contract = Files.readString(OPENAPI_PATH);

        assertAll(
                () -> assertTrue(contract.contains("/api/auth/login:")),
                () -> assertTrue(contract.contains("operationId: loginUser")),
                () -> assertTrue(contract.contains("#/components/schemas/LoginRequest")),
                () -> assertTrue(contract.contains("#/components/schemas/SessionResponse")),
                () -> assertTrue(contract.contains("INVALID_CREDENTIALS")),
                () -> assertTrue(contract.contains("ACCOUNT_INACTIVE"))
        );
    }
}
