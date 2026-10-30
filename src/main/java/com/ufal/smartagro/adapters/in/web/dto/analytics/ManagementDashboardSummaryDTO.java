package com.ufal.smartagro.adapters.in.web.dto.analytics;

import com.ufal.smartagro.domain.model.enums.ManagementDashboardScope;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Resumo consolidado do dashboard macro-gerencial (Administrador/Gestor).
 */
public record ManagementDashboardSummaryDTO(
        ManagementDashboardScope scope,
        UUID organizationId,
        String organizationName,
        HarvestSummaryInfoDTO currentHarvest,
        UUID appliedCommunityId,
        boolean isEmpty,
        int totalOrganizationsCount,
        int totalCommunitiesCount,
        int totalFarmersCount,
        int compliantFarmersCount,
        BigDecimal complianceRatePct,
        int totalPlansCount,
        BigDecimal totalPlantedAreaHa,
        BigDecimal totalTargetKg,
        BigDecimal totalRealizedKg,
        BigDecimal overallConversionRatePct,
        List<ManagementCropRankingItemDTO> cropRanking,
        List<ManagementCommunityPerformanceDTO> communities,
        List<ManagementOrganizationSummaryDTO> organizations
) {
    /**
     * Fábrica de conveniência para estado vazio (Empty State).
     */
    public static ManagementDashboardSummaryDTO empty(
            ManagementDashboardScope scope,
            UUID organizationId,
            String organizationName,
            HarvestSummaryInfoDTO currentHarvest,
            UUID appliedCommunityId
    ) {
        return new ManagementDashboardSummaryDTO(
                scope,
                organizationId,
                organizationName,
                currentHarvest,
                appliedCommunityId,
                true,
                0,
                0,
                0,
                0,
                BigDecimal.ZERO.setScale(2),
                0,
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
