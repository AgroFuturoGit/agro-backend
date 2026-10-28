package com.ufal.smartagro.adapters.in.web.dto.production.occurrence;

import com.ufal.smartagro.domain.model.enums.OccurrenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record FieldOccurrenceRegisterDTO(
        @Schema(description = "Identificador gerado no cliente offline para deduplicação idempotente", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID clientId,

        @NotNull(message = "O tipo de ocorrência é obrigatório")
        @Schema(description = "Tipo da ocorrência de campo", example = "PEST")
        OccurrenceType logType,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        @Schema(description = "Descrição detalhada do fato ocorrido", example = "Incidência de lagarta do cartucho na área sul")
        String description,

        @Size(max = 255, message = "A observação climática deve ter no máximo 255 caracteres")
        @Schema(description = "Observação climática no momento do registro", example = "Sol forte, 32°C")
        String weatherObservation,

        @Size(max = 500, message = "O link de evidência deve ter no máximo 500 caracteres")
        @Schema(description = "Link para evidência fotográfica externa ou relatório", example = "https://storage.smartagro.test/evidence/123.jpg")
        String evidenceUrl
) {
    /** Construtor de compatibilidade para chamadas sem clientId. */
    public FieldOccurrenceRegisterDTO(OccurrenceType logType, String description, String weatherObservation, String evidenceUrl) {
        this(null, logType, description, weatherObservation, evidenceUrl);
    }

    /** Cria nova instância vinculando ou substituindo o clientId (ex: extraído de Idempotency-Key). */
    public FieldOccurrenceRegisterDTO withClientId(UUID resolvedClientId) {
        return new FieldOccurrenceRegisterDTO(
                resolvedClientId,
                this.logType,
                this.description,
                this.weatherObservation,
                this.evidenceUrl
        );
    }
}
