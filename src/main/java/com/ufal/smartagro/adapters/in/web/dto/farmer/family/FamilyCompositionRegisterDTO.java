package com.ufal.smartagro.adapters.in.web.dto.farmer.family;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FamilyCompositionRegisterDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        @Schema(description = "Nome do membro familiar", example = "Maria da Silva")
        String name,

        @NotBlank(message = "O parentesco é obrigatório")
        @Size(max = 50, message = "O parentesco deve ter no máximo 50 caracteres")
        @Schema(description = "Grau de parentesco", example = "Esposa")
        String kinship,

        @NotNull(message = "A idade é obrigatória")
        @Min(value = 0, message = "A idade não pode ser negativa")
        @Max(value = 150, message = "A idade não pode ser superior a 150 anos")
        @Schema(description = "Idade em anos", example = "42")
        Integer age
) {
}
