package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductionExecutionRegisterDTO(
        @NotNull(message = "A quantidade real produzida é obrigatória.")
        @DecimalMin(value = "0.01", message = "A quantidade produzida deve ser maior que zero.")
        BigDecimal actualYield,

        @NotNull(message = "A data da colheita é obrigatória.")
        LocalDate harvestDate,

        /**
         * Onde o apontamento foi feito. Opcional: o aparelho pode estar sem
         * sinal de GPS ou com a permissão negada, e isso não pode impedir o
         * registro da produção.
         */
        @DecimalMin(value = "-90.0", message = "Latitude inválida.")
        @DecimalMax(value = "90.0", message = "Latitude inválida.")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude inválida.")
        @DecimalMax(value = "180.0", message = "Longitude inválida.")
        BigDecimal longitude,

        /** Raio de erro da leitura, em metros, como informado pelo GPS. */
        @DecimalMin(value = "0.0", message = "A precisão não pode ser negativa.")
        BigDecimal locationAccuracy,

        /** Quando o GPS obteve a posição. */
        LocalDateTime locationRecordedAt
) {
}
