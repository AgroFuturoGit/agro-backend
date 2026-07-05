package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductionExecutionUpdateDTO(
        @DecimalMin(value = "0.01", message = "A quantidade produzida deve ser maior que zero.")
        BigDecimal actualYield,

        LocalDate harvestDate
) {
}
