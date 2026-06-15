package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductionExecutionRegisterDTO(
        @NotNull(message = "O rendimento real é obrigatório.")
        @DecimalMin(value = "0.00", message = "O rendimento real não pode ser negativo.")
        BigDecimal actualYield,

        @NotNull(message = "A data de registro é obrigatória.")
        LocalDateTime recordedAt
) {
}
