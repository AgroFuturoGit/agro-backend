package com.ufal.smartagro.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class FinancingTest {
    @Test void preservesMonetaryAmountAndOptionalAuditFields() {
        Financing value = Financing.builder().description("PRONAF").status("ACTIVE").amount(new BigDecimal("12345.67")).build();
        assertThat(value.getAmount()).isEqualByComparingTo("12345.67"); assertThat(value.getUpdatedAt()).isNull(); assertThat(value.getDeletedAt()).isNull();
    }
}
