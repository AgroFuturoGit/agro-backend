package com.ufal.smartagro.adapters.in.web.dto.production;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductionComparisonDTO(
        UUID productionPlanId,
        BigDecimal expectedYield,
        BigDecimal totalActualYield,
        BigDecimal difference,
        BigDecimal percentageRealized
) {
}
