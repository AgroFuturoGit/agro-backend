package com.ufal.smartagro.adapters.in.web.dto.crop;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;

/** Dados editáveis de uma cultura para atualização parcial (PATCH). */
public record CropUpdateDTO(
        String name,
        String variety,
        Boolean isPriority,
        Integer cycleDays,
        @Schema(description = "Produtividade esperada em quilogramas por hectare (kg/ha).")
        BigDecimal expectedProductivity,
        HarvestType harvestType,
        MeasurementUnit unit,
        BigDecimal unitWeightKg
) {
}
