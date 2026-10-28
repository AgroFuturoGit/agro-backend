package com.ufal.smartagro.adapters.in.web.dto.crop;

import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Dados editáveis de uma cultura para atualização parcial (PATCH), com suporte a controle de versão concorrente. */
public record CropUpdateDTO(
        String name,
        String variety,
        Boolean isPriority,
        Integer cycleDays,

        @Schema(description = "Produtividade esperada em kg/ha", example = "12000.00")
        BigDecimal expectedProductivity,

        HarvestType harvestType,
        MeasurementUnit unit,
        BigDecimal unitWeightKg,
        LocalDateTime baseUpdatedAt
) {
    public CropUpdateDTO(String name, String variety, Boolean isPriority, Integer cycleDays,
                         BigDecimal expectedProductivity, HarvestType harvestType,
                         MeasurementUnit unit, BigDecimal unitWeightKg) {
        this(name, variety, isPriority, cycleDays, expectedProductivity, harvestType, unit, unitWeightKg, null);
    }
}
