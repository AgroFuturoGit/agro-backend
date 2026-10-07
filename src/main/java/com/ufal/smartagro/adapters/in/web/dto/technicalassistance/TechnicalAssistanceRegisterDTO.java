package com.ufal.smartagro.adapters.in.web.dto.technicalassistance;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record TechnicalAssistanceRegisterDTO(
        UUID technicianId,
        @NotNull(message = "O ID da comunidade é obrigatório") UUID communityId,
        @NotNull(message = "A data de início é obrigatória") LocalDateTime startDate
) {}
