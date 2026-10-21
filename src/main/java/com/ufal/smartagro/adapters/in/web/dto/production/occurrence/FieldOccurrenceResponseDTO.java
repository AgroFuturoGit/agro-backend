package com.ufal.smartagro.adapters.in.web.dto.production.occurrence;

import com.ufal.smartagro.domain.model.enums.OccurrenceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public record FieldOccurrenceResponseDTO(
        @Schema(description = "Identificador único da ocorrência", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID id,

        @Schema(description = "Identificador do apontamento de produção vinculado", example = "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22")
        UUID executionId,

        @Schema(description = "Tipo de ocorrência", example = "PEST")
        OccurrenceType logType,

        @Schema(description = "Descrição do fato", example = "Incidência de lagarta do cartucho na área sul")
        String description,

        @Schema(description = "Observação do clima", example = "Sol forte, 32°C")
        String weatherObservation,

        @Schema(description = "URL da evidência", example = "https://storage.smartagro.test/evidence/123.jpg")
        String evidenceUrl,

        @Schema(description = "Data de cadastro")
        LocalDateTime createdAt,

        @Schema(description = "Data da última atualização")
        LocalDateTime updatedAt
) {
}
