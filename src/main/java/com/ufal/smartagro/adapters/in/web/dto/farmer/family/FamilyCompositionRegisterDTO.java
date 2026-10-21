package com.ufal.smartagro.adapters.in.web.dto.farmer.family;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FamilyCompositionRegisterDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Schema(description = "Nome do membro familiar", example = "Maria da Silva")
        String name,

        @NotBlank(message = "O parentesco é obrigatório")
        @Schema(description = "Grau de parentesco", example = "Esposa")
        String kinship,

        @NotNull(message = "A idade é obrigatória")
        @Min(value = 0, message = "A idade não pode ser negativa")
        @Schema(description = "Idade em anos", example = "42")
        Integer age
) {
}
