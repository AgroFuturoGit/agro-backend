package com.ufal.smartagro.adapters.in.web.dto.production;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Representação do apontamento devolvida pela API, com dados de colheita e validação. */
public record ProductionExecutionResponseDTO(
        UUID id,
        UUID productionPlanId,
        BigDecimal quantity,
        BigDecimal quantityKg,
        LocalDateTime harvestedAt,
        com.ufal.smartagro.domain.model.enums.ExecutionStatus status,
        String validatedBy,
        LocalDateTime validatedAt,
        String notes,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal locationAccuracy,
        LocalDateTime locationRecordedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
