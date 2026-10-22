package com.ufal.smartagro.adapters.in.web.dto.analytics;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Agregação de métricas por comunidade assistida pelo técnico.
 */
public record TechnicianCommunitySummaryDTO(
        UUID communityId,
        String communityName,
        int assistedFarmersCount,
        int farmersWithPlansCount,
        int plansCount,
        BigDecimal plantedAreaHa,
        BigDecimal targetKg,
        BigDecimal realizedKg,
        BigDecimal pendingKg,
        BigDecimal completionPct,
        int warningsCount
) {
}
