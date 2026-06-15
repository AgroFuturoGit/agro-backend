package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ProductionPlanRegisterDTO(
        @NotNull(message = "A safra é obrigatória.")
        UUID harvestId,

        @NotNull(message = "O cultivo é obrigatório.")
        UUID cropId,

        @NotNull(message = "A área de plantio é obrigatória.")
        @DecimalMin(value = "0.01", message = "A área de plantio deve ser maior que zero.")
        BigDecimal plantedArea,

        @NotNull(message = "O rendimento esperado é obrigatório.")
        @DecimalMin(value = "0.01", message = "O rendimento esperado deve ser maior que zero.")
        BigDecimal expectedYield,

        Map<String, Object> plannedCalendar
) {
}
