package com.ufal.smartagro.adapters.in.web.dto.production.occurrence;

import com.ufal.smartagro.domain.model.enums.OccurrenceType;
import io.swagger.v3.oas.annotations.media.Schema;

public record FieldOccurrenceUpdateDTO(
        @Schema(description = "Tipo da ocorrência de campo", example = "PEST")
        OccurrenceType logType,

        @Schema(description = "Descrição detalhada do fato ocorrido", example = "Incidência controlada de lagarta do cartucho")
        String description,

        @Schema(description = "Observação climática", example = "Chuva leve, 24°C")
        String weatherObservation,

        @Schema(description = "Link para evidência fotográfica externa", example = "https://storage.smartagro.test/evidence/124.jpg")
        String evidenceUrl
) {
}
