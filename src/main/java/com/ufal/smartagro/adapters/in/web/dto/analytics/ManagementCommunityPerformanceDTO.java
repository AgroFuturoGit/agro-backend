package com.ufal.smartagro.adapters.in.web.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Indicadores consolidados de desempenho e adimplência por comunidade atendida.
 */
public record ManagementCommunityPerformanceDTO(
        UUID communityId,
        String communityName,
        UUID organizationId,
        String organizationName,
        int activeFarmersCount,
        int compliantFarmersCount,
        BigDecimal complianceRatePct,
        int plansCount,
        BigDecimal plantedAreaHa,
        BigDecimal targetKg,
        BigDecimal realizedKg,
        BigDecimal conversionRatePct,
        LocalDate lastExecutionDate
) {
}
