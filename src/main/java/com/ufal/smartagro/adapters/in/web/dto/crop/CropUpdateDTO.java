package com.ufal.smartagro.adapters.in.web.dto.crop;

import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** Dados editáveis de uma cultura para atualização parcial (PATCH). */
public record CropUpdateDTO(
        String name,
        String variety,
        Boolean isPriority,
        Integer cycleDays,

        @Schema(description = "Produtividade esperada em kg/ha", example = "12000.00")
        BigDecimal expectedProductivity,

        HarvestType harvestType,
        MeasurementUnit unit,
        BigDecimal unitWeightKg
) {
}
