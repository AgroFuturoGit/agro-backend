package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record ProductionPlanUpdateDTO(
        @DecimalMin(value = "0.01", message = "A área de plantio deve ser maior que zero.")
        BigDecimal plantedArea,

        @DecimalMin(value = "0.01", message = "O rendimento esperado deve ser maior que zero.")
        BigDecimal expectedYield,

        LocalDate plannedPlantingDate,

        Map<String, Object> plannedCalendar
) {
}
