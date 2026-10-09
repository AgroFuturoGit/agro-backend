package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Dados de um apontamento de colheita; o estado inicial de validação é definido pelo serviço. */
public record ProductionExecutionRegisterDTO(
        @NotNull(message = "A quantidade colhida é obrigatória.") @DecimalMin("0.01") BigDecimal quantity,
        @NotNull(message = "A quantidade em kg é obrigatória.") @DecimalMin("0.01") BigDecimal quantityKg,
        @NotNull(message = "A data e hora da colheita são obrigatórias.") LocalDateTime harvestedAt,
        String notes,

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
    /** Converte a assinatura antiga para os novos campos, tratando a antiga data como início do dia. */
    public ProductionExecutionRegisterDTO(BigDecimal actualYield, LocalDate harvestDate, BigDecimal latitude,
                                          BigDecimal longitude, BigDecimal locationAccuracy, LocalDateTime locationRecordedAt) {
        this(actualYield, actualYield, harvestDate == null ? null : harvestDate.atStartOfDay(), null,
                latitude, longitude, locationAccuracy, locationRecordedAt);
    }
}
