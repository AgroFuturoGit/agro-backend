package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductionExecutionUpdateDTO(
        @DecimalMin(value = "0.01", message = "A quantidade produzida deve ser maior que zero.")
        BigDecimal actualYield,

        LocalDate harvestDate,

        @DecimalMin(value = "-90.0", message = "Latitude inválida.")
        @DecimalMax(value = "90.0", message = "Latitude inválida.")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude inválida.")
        @DecimalMax(value = "180.0", message = "Longitude inválida.")
        BigDecimal longitude,

        @DecimalMin(value = "0.0", message = "A precisão não pode ser negativa.")
        BigDecimal locationAccuracy,

        LocalDateTime locationRecordedAt,

        /**
         * Remove a localização do apontamento. Necessário porque um campo nulo
         * significa "não mexer", então sem este sinal não haveria como apagar
         * uma posição já gravada.
         */
        Boolean clearLocation
) {
}
