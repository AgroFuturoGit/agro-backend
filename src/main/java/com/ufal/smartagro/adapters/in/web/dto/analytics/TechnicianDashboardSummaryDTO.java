package com.ufal.smartagro.adapters.in.web.dto.analytics;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Resumo consolidado do dashboard da carteira do técnico.
 */
public record TechnicianDashboardSummaryDTO(
        UUID technicianId,
        String technicianName,
        HarvestSummaryInfoDTO currentHarvest,
        UUID appliedCommunityId,
        UUID appliedCropId,
        boolean isEmpty,
        int totalAssistedCommunitiesCount,
        int totalAssistedFarmersCount,
        int totalPlansCount,
        int activePlansCount,
        BigDecimal totalPlantedAreaHa,
        BigDecimal totalTargetKg,
        BigDecimal totalRealizedKg,
        BigDecimal totalPendingKg,
        int pendingExecutionsCount,
        BigDecimal overallCompletionPct,
        int totalWarningsCount,
        List<TechnicianPlanWarningDTO> warnings,
        List<TechnicianCommunitySummaryDTO> communities,
        List<TechnicianFarmerSummaryDTO> farmers
) {
    /**
     * Fábrica de conveniência para estado vazio (Empty State).
     */
    public static TechnicianDashboardSummaryDTO empty(
            UUID technicianId,
            String technicianName,
            HarvestSummaryInfoDTO currentHarvest,
            UUID appliedCommunityId,
            UUID appliedCropId
    ) {
        return new TechnicianDashboardSummaryDTO(
                technicianId,
                technicianName,
                currentHarvest,
                appliedCommunityId,
                appliedCropId,
                true,
                0,
                0,
                0,
                0,
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                0,
                BigDecimal.ZERO.setScale(2),
                0,
                List.of(),
                List.of(),
                List.of()
        );
    }
}
