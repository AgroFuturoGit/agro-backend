package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.FinancingStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FinancingTest {

    @Test
    void deveCriarFinancingCorretamente() {
        UUID id = UUID.randomUUID();
        Farmer farmer = Farmer.builder().id(UUID.randomUUID()).build();
        BigDecimal amount = new BigDecimal("25000.00");
        LocalDateTime now = LocalDateTime.now();

        Financing financing = Financing.builder()
                .id(id)
                .farmer(farmer)
                .description("PRONAF Custeio 2026")
                .status(FinancingStatus.APPROVED)
                .amount(amount)
                .createdAt(now)
                .build();

        assertThat(financing.getId()).isEqualTo(id);
        assertThat(financing.getFarmer()).isEqualTo(farmer);
        assertThat(financing.getDescription()).isEqualTo("PRONAF Custeio 2026");
        assertThat(financing.getStatus()).isEqualTo(FinancingStatus.APPROVED);
        assertThat(financing.getAmount()).isEqualByComparingTo(amount);
        assertThat(financing.getCreatedAt()).isEqualTo(now);
    }
}
