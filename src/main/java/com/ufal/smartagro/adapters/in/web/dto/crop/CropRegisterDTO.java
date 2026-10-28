package com.ufal.smartagro.adapters.in.web.dto.crop;

import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Dados de entrada para cadastrar uma cultura, incluindo ciclo, produtividade e unidade de medida. */
public record CropRegisterDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String name,

        @NotBlank(message = "A variedade é obrigatória")
        @Size(max = 100, message = "A variedade deve ter no máximo 100 caracteres")
        String variety,

        @NotNull(message = "O campo isPriority é obrigatório")
        Boolean isPriority,

        @Positive(message = "O ciclo de dias deve ser positivo")
        @Max(value = 1000, message = "O ciclo de dias não pode exceder 1000")
        Integer cycleDays,

        @Schema(description = "Produtividade esperada em kg/ha", example = "12000.00")
        @Positive(message = "A produtividade esperada deve ser positiva")
        @Digits(integer = 8, fraction = 2, message = "A produtividade esperada deve ter no máximo 8 dígitos inteiros e 2 decimais")
        BigDecimal expectedProductivity,

        HarvestType harvestType,
        MeasurementUnit unit,

        @Positive(message = "O peso unitário deve ser positivo")
        @Digits(integer = 6, fraction = 3, message = "O peso unitário deve ter no máximo 6 dígitos inteiros e 3 decimais")
        BigDecimal unitWeightKg
) {
}
