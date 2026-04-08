package com.bankapp.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.bankapp.auth.domain.User;
import com.bankapp.auth.domain.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SecurityRegressionTest {

    @Test
    void userSerializationDoesNotLeakPasswordHash() throws Exception {
        User user = new User("customer@example.com", "hashed-secret", Set.of(UserRole.CUSTOMER), true);
        String json = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writeValueAsString(user);

        assertFalse(json.contains("passwordHash"));
        assertFalse(json.contains("hashed-secret"));
    }

    @Test
    void backendSourcesDoNotLogPlaintextPasswords() throws IOException {
        Path sourceRoot = Path.of("src", "main", "java");
        assertNotNull(sourceRoot);

        try (var files = Files.walk(sourceRoot)) {
            boolean found = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readUnchecked)
                    .anyMatch(content -> content.matches("(?s).*(log\\.|System\\.out|System\\.err).*(?i)password.*"));

            assertFalse(found);
        }
    }

    private String readUnchecked(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
