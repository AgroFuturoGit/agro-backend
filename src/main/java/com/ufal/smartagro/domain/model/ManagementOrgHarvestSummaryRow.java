package com.ufal.smartagro.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representa uma linha pré-agregada da view materializada mv_org_harvest_summary.
 */
public record ManagementOrgHarvestSummaryRow(
        UUID organizationId,
        String organizationName,
        UUID communityId,
        String communityName,
        UUID cropId,
        String cropName,
        String cropVariety,
        String unit,
        UUID harvestId,
        long planCount,
        long farmerCount,
        BigDecimal plantedAreaHa,
        BigDecimal targetKg,
        BigDecimal realizedKg,
        LocalDate lastExecutionDate
) {
}
