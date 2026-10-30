package com.ufal.smartagro.adapters.in.web.dto.production.occurrence;

import com.ufal.smartagro.domain.model.enums.OccurrenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record FieldOccurrenceUpdateDTO(
        @Schema(description = "Tipo da ocorrência de campo", example = "PEST")
        OccurrenceType logType,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        @Schema(description = "Descrição detalhada do fato ocorrido", example = "Incidência controlada de lagarta do cartucho")
        String description,

        @Size(max = 255, message = "A observação climática deve ter no máximo 255 caracteres")
        @Schema(description = "Observação climática", example = "Chuva leve, 24°C")
        String weatherObservation,

        @Size(max = 500, message = "O link para evidência fotográfica deve ter no máximo 500 caracteres")
        @Schema(description = "Link para evidência fotográfica externa", example = "https://storage.smartagro.test/evidence/124.jpg")
        String evidenceUrl
) {
}
