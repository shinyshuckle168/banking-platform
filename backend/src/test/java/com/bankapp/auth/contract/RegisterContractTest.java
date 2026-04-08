package com.bankapp.auth.contract;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RegisterContractTest {

    private static final Path OPENAPI_PATH = Path.of("..", "specs", "001-bank-auth-customer", "contracts", "openapi.yaml");

    @Test
    void registerContractDefinesExpectedRouteAndResponses() throws IOException {
        String contract = Files.readString(OPENAPI_PATH);

        assertAll(
                () -> assertTrue(contract.contains("/api/auth/register:")),
                () -> assertTrue(contract.contains("operationId: registerUser")),
                () -> assertTrue(contract.contains("'201':")),
                () -> assertTrue(contract.contains("'409':")),
                () -> assertTrue(contract.contains("'422':")),
                () -> assertTrue(contract.contains("USER_ALREADY_EXISTS")),
                () -> assertTrue(contract.contains("INVALID_PASSWORD_FORMAT")),
                () -> assertTrue(contract.contains("#/components/schemas/RegisterRequest")),
                () -> assertTrue(contract.contains("#/components/schemas/UserResponse"))
        );
    }
}
