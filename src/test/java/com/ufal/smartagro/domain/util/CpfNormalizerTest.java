package com.ufal.smartagro.domain.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CpfNormalizerTest {

    @ParameterizedTest(name = "Normalização de ''{0}'' deve resultar em ''{1}''")
    @CsvSource(value = {
            "'123.456.789-00', '12345678900'",
            "'12345678900', '12345678900'",
            "' 123.456.789-00 ', '12345678900'",
            "'abc123def456ghi789j00', '12345678900'"
    })
    void givenCpfWithFormatting_whenDigitsOnly_thenReturnsDigitsOnly(String input, String expected) {
        assertThat(CpfNormalizer.digitsOnly(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Quando entrada é nula, retorna nulo")
    void givenNullInput_whenDigitsOnly_thenReturnsNull() {
        assertThat(CpfNormalizer.digitsOnly(null)).isNull();
    }
}
