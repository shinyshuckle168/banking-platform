package com.fdm.banking.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Mod97Validator. (T021)
 */
class Mod97ValidatorTest {

    private final Mod97Validator validator = new Mod97Validator();

    @Test
    void validIban_returnsTrue() {
        // GB82WEST12345698765432 is a standard test IBAN
        assertThat(validator.isValid("GB82WEST12345698765432", null)).isTrue();
    }

    @Test
    void invalidIban_returnsFalse() {
        assertThat(validator.isValid("GB00WEST12345698765432", null)).isFalse();
    }

    @Test
    void null_returnsFalse() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @Test
    void tooShort_returnsFalse() {
        assertThat(validator.isValid("GB82", null)).isFalse();
    }

    @Test
    void tooLong_returnsFalse() {
        String longIban = "A".repeat(35);
        assertThat(validator.isValid(longIban, null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "DE89370400440532013000",
        "FR7614508711001411943361170"
    })
    void validEuropeanIbans_returnTrue(String iban) {
        assertThat(validator.isValid(iban, null)).isTrue();
    }
}
