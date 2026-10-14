package com.ufal.smartagro.adapters.in.web.dto.crop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Dados editáveis de uma cultura; os campos agrícolas complementam nome e variedade. */
public record CropUpdateDTO(
        @NotBlank(message = "O nome é obrigatório")
        String name,

        @NotBlank(message = "A variedade é obrigatória")
        String variety,

        @NotNull(message = "O campo isPriority é obrigatório")
        Boolean isPriority,
        Integer cycleDays,
        java.math.BigDecimal expectedProductivity,
        com.ufal.smartagro.domain.model.enums.HarvestType harvestType,
        com.ufal.smartagro.domain.model.enums.MeasurementUnit unit,
        java.math.BigDecimal unitWeightKg
) {
}
