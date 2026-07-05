package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductionExecutionRegisterDTO(
        @NotNull(message = "A quantidade real produzida é obrigatória.")
        @DecimalMin(value = "0.01", message = "A quantidade produzida deve ser maior que zero.")
        BigDecimal actualYield,

        @NotNull(message = "A data da colheita é obrigatória.")
        LocalDate harvestDate
) {
}
