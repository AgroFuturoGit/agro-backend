package com.ufal.smartagro.adapters.in.web.dto.analytics;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Resumo geral do dashboard individual do agricultor.
 */
public record FarmerDashboardSummaryDTO(
        UUID farmerId,
        String farmerName,
        String communityName,
        HarvestSummaryInfoDTO currentHarvest,
        boolean isEmpty,
        int totalPlansCount,
        int activePlotsCount,
        BigDecimal totalPlantedAreaHa,
        BigDecimal totalTargetKg,
        BigDecimal totalRealizedKg,
        BigDecimal totalPendingKg,
        BigDecimal overallCompletionPct,
        List<FarmerPlanSummaryCardDTO> plans
) {
    /**
     * Fábrica de conveniência para estado vazio (Empty State).
     */
    public static FarmerDashboardSummaryDTO empty(
            UUID farmerId,
            String farmerName,
            String communityName,
            HarvestSummaryInfoDTO currentHarvest
    ) {
        return new FarmerDashboardSummaryDTO(
                farmerId,
                farmerName,
                communityName,
                currentHarvest,
                true,
                0,
                0,
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                List.of()
        );
    }
}
