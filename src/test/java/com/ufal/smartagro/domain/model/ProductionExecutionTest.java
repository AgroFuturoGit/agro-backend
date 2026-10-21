package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class ProductionExecutionTest {
    @Test void deveManterQuantidadeUnidadeKgEDataDaColheita() {
        LocalDate date = LocalDate.of(2026, 5, 10);
        ProductionExecution execution = execution(ExecutionStatus.PENDING, new BigDecimal("5"), new BigDecimal("300"), date, null, null, null);
        assertThat(execution.getQuantity()).isEqualByComparingTo("5");
        assertThat(execution.getQuantityKg()).isEqualByComparingTo("300");
        assertThat(execution.getHarvestedAt()).isEqualTo(date);
    }
    @Test void deveManterEstadoETrilhaDeValidacao() {
        User validator = org.mockito.Mockito.mock(User.class);
        LocalDateTime at = LocalDateTime.of(2026, 5, 11, 10, 30);
        ProductionExecution execution = execution(ExecutionStatus.VALIDATED, BigDecimal.ONE, BigDecimal.ONE, LocalDate.now(), validator, at, "Conferido");
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.VALIDATED);
        assertThat(execution.getValidatedBy()).isSameAs(validator);
        assertThat(execution.getValidatedAt()).isEqualTo(at);
        assertThat(execution.getNotes()).isEqualTo("Conferido");
    }
    @Test void devePermitirEstadosPendingValidatedERejected() {
        for (ExecutionStatus status : ExecutionStatus.values()) assertThat(execution(status, BigDecimal.ONE, BigDecimal.ONE, LocalDate.now(), null, null, null).getStatus()).isEqualTo(status);
    }
    @Test void deveExporCompatibilidadeDeActualYieldComoQuantityKg() {
        ProductionExecution execution = execution(ExecutionStatus.PENDING, BigDecimal.ONE, new BigDecimal("75"), LocalDate.now(), null, null, null);
        assertThat(execution.getActualYield()).isEqualByComparingTo("75");
    }
    @Test void deveManterRejectionReason() {
        ProductionExecution execution = new ProductionExecution(
                UUID.randomUUID(), null, BigDecimal.ONE, BigDecimal.ONE, LocalDate.now(),
                ExecutionStatus.REJECTED, null, null, "Observações gerais", "Produto fora do padrão",
                null, null, null, null, null, null, null
        );
        assertThat(execution.getRejectionReason()).isEqualTo("Produto fora do padrão");
        assertThat(execution.getNotes()).isEqualTo("Observações gerais");
    }
    private ProductionExecution execution(ExecutionStatus status, BigDecimal quantity, BigDecimal kg, LocalDate date, User validator, LocalDateTime at, String notes) {
        return new ProductionExecution(UUID.randomUUID(), null, quantity, kg, date, status, validator, at, notes,
                null, null, null, null, null, null, null);
    }
}
