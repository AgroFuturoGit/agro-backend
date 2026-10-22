package com.ufal.smartagro.adapters.in.web.dto.analytics;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Item do ranking de culturas com volume, conversão e participação de mercado (market share).
 */
public record ManagementCropRankingItemDTO(
        int rankPosition,
        UUID cropId,
        String cropName,
        String cropVariety,
        String unit,
        int plansCount,
        BigDecimal plantedAreaHa,
        BigDecimal targetKg,
        BigDecimal realizedKg,
        BigDecimal conversionRatePct,
        BigDecimal productionSharePct
) {
}
