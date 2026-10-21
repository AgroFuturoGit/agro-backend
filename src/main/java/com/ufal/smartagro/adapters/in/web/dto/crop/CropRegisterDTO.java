package com.ufal.smartagro.adapters.in.web.dto.crop;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Dados de entrada para cadastrar uma cultura, incluindo ciclo, produtividade e unidade de medida. */
public record CropRegisterDTO(
        @NotBlank(message = "O nome é obrigatório")
        String name,

        @NotBlank(message = "A variedade é obrigatória")
        String variety,

        @NotNull(message = "O campo isPriority é obrigatório")
        Boolean isPriority,
        Integer cycleDays,

        @Schema(description = "Produtividade esperada em kg/ha", example = "12000.00")
        BigDecimal expectedProductivity,

        com.ufal.smartagro.domain.model.enums.HarvestType harvestType,
        com.ufal.smartagro.domain.model.enums.MeasurementUnit unit,
        BigDecimal unitWeightKg
) {
}
