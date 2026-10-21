package com.ufal.smartagro.adapters.in.web.dto.farmer.family;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public record FamilyCompositionResponseDTO(
        @Schema(description = "Identificador único do membro da composição familiar", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID id,

        @Schema(description = "Identificador do agricultor vinculado", example = "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22")
        UUID farmerId,

        @Schema(description = "Nome do membro", example = "Maria da Silva")
        String name,

        @Schema(description = "Parentesco com o agricultor", example = "Esposa")
        String kinship,

        @Schema(description = "Idade em anos", example = "42")
        Integer age,

        @Schema(description = "Data de cadastro")
        LocalDateTime createdAt,

        @Schema(description = "Data da última atualização")
        LocalDateTime updatedAt
) {
}
