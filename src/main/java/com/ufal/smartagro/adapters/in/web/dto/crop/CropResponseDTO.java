package com.ufal.smartagro.adapters.in.web.dto.crop;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

/** Representação da cultura devolvida pela API, com seus parâmetros de ciclo e colheita. */
public record CropResponseDTO(
        UUID id,
        String name,
        String variety,
        Boolean isPriority,
        Integer cycleDays,

        @Schema(description = "Produtividade esperada em kg/ha", example = "12000.00")
        BigDecimal expectedProductivity,

        com.ufal.smartagro.domain.model.enums.HarvestType harvestType,
        com.ufal.smartagro.domain.model.enums.MeasurementUnit unit,
        BigDecimal unitWeightKg
) {
}
