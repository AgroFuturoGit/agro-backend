package com.ufal.smartagro.adapters.in.web.dto.production;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductionExecutionResponseDTO(
        UUID id,
        UUID productionPlanId,
        BigDecimal actualYield,
        LocalDateTime recordedAt,
        LocalDateTime createdAt
) {
}
