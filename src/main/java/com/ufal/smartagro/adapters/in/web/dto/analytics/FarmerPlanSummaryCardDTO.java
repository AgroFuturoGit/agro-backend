package com.ufal.smartagro.adapters.in.web.dto.analytics;

import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.PlanProgressStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resumo consolidado do plano/talhão para o cartão do agricultor.
 */
public record FarmerPlanSummaryCardDTO(
        UUID planId,
        UUID cropId,
        String cropName,
        String cropVariety,
        HarvestType harvestType,
        String unit,
        PlanStatus status,
        String locationDescription,
        BigDecimal plantedAreaHa,
        BigDecimal targetKg,
        BigDecimal realizedKg,
        BigDecimal pendingKg,
        BigDecimal completionPct,
        PlanProgressStatus progressStatus,
        LocalDate plantingDate,
        LocalDate expectedHarvestStart,
        LocalDate expectedHarvestEnd,
        LocalDate lastExecutionDate
) {
}
