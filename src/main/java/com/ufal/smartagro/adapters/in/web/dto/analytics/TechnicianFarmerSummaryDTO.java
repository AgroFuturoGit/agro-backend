package com.ufal.smartagro.adapters.in.web.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resumo consolidado do desempenho de um agricultor assistido pelo técnico.
 */
public record TechnicianFarmerSummaryDTO(
        UUID farmerId,
        String farmerName,
        UUID communityId,
        String communityName,
        boolean isCompliant,
        int plansCount,
        int activePlansCount,
        BigDecimal plantedAreaHa,
        BigDecimal targetKg,
        BigDecimal realizedKg,
        BigDecimal pendingKg,
        BigDecimal completionPct,
        LocalDate lastExecutionDate,
        int warningsCount
) {
}
