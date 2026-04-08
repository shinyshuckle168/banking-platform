package com.bankapp.customer.contract;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GetCustomerContractTest {

    private static final Path OPENAPI_PATH = Path.of("..", "specs", "001-bank-auth-customer", "contracts", "openapi.yaml");

    @Test
    void getCustomerContractIncludesAuthAndNotFoundResponses() throws IOException {
        String contract = Files.readString(OPENAPI_PATH);

        assertAll(
                () -> assertTrue(contract.contains("operationId: getCustomer")),
                () -> assertTrue(contract.contains("Customer detail response")),
                () -> assertTrue(contract.contains("'404':")),
                () -> assertTrue(contract.contains("'401':"))
        );
    }
}
