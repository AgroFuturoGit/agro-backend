package com.ufal.smartagro.adapters.in.web.dto.technicalassistance;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record TechnicalAssistanceRegisterDTO(
        @NotNull(message = "O ID do técnico é obrigatório") UUID technicianId,
        @NotNull(message = "O ID do produtor é obrigatório") UUID producerId,
        @NotNull(message = "A data de início é obrigatória") LocalDateTime startDate
) {}
