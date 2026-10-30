package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.PlanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Projeção de domínio com os dados agregados do plano e seus apontamentos para o dashboard.
 */
public record FarmerPlanSummary(
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
        LocalDate plantingDate,
        LocalDate expectedHarvestStart,
        LocalDate expectedHarvestEnd,
        LocalDate lastExecutionDate
) {
}
