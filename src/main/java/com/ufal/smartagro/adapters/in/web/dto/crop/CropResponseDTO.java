package com.ufal.smartagro.adapters.in.web.dto.crop;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

/** Representação da cultura devolvida pela API, com seus parâmetros de ciclo e colheita. */
public record CropResponseDTO(
        UUID id,
        String name,
        String variety,
        Boolean isPriority,
        Integer cycleDays,
        @Schema(description = "Produtividade esperada em quilogramas por hectare (kg/ha).")
        java.math.BigDecimal expectedProductivity,
        com.ufal.smartagro.domain.model.enums.HarvestType harvestType,
        com.ufal.smartagro.domain.model.enums.MeasurementUnit unit,
        java.math.BigDecimal unitWeightKg
) {
}
