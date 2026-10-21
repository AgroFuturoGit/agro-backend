package com.ufal.smartagro.adapters.in.web.dto.production.occurrence;

import com.ufal.smartagro.domain.model.enums.OccurrenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FieldOccurrenceRegisterDTO(
        @NotNull(message = "O tipo de ocorrência é obrigatório")
        @Schema(description = "Tipo da ocorrência de campo", example = "PEST")
        OccurrenceType logType,

        @NotBlank(message = "A descrição é obrigatória")
        @Schema(description = "Descrição detalhada do fato ocorrido", example = "Incidência de lagarta do cartucho na área sul")
        String description,

        @Schema(description = "Observação climática no momento do registro", example = "Sol forte, 32°C")
        String weatherObservation,

        @Schema(description = "Link para evidência fotográfica externa ou relatório", example = "https://storage.smartagro.test/evidence/123.jpg")
        String evidenceUrl
) {
}
