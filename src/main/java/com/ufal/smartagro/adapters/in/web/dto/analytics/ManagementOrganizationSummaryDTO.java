package com.ufal.smartagro.adapters.in.web.dto.analytics;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Indicadores consolidados de desempenho por organização.
 */
public record ManagementOrganizationSummaryDTO(
        UUID organizationId,
        String organizationName,
        int communitiesCount,
        int activeFarmersCount,
        int compliantFarmersCount,
        BigDecimal complianceRatePct,
        int plansCount,
        BigDecimal plantedAreaHa,
        BigDecimal targetKg,
        BigDecimal realizedKg,
        BigDecimal conversionRatePct
) {
}
