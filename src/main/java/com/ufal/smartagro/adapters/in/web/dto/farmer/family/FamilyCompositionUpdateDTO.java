package com.ufal.smartagro.adapters.in.web.dto.farmer.family;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

public record FamilyCompositionUpdateDTO(
        @Schema(description = "Nome do membro familiar", example = "Maria da Silva")
        String name,

        @Schema(description = "Grau de parentesco", example = "Esposa")
        String kinship,

        @Min(value = 0, message = "A idade não pode ser negativa")
        @Schema(description = "Idade em anos", example = "43")
        Integer age
) {
}
