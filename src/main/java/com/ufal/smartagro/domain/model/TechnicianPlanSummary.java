package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.PlanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Projeção de domínio com os dados agregados dos planos da carteira do técnico.
 */
public record TechnicianPlanSummary(
        UUID planId,
        UUID farmerId,
        String farmerName,
        UUID communityId,
        String communityName,
        UUID cropId,
        String cropName,
        String cropVariety,
        HarvestType harvestType,
        String unit,
        PlanStatus status,
        BigDecimal plantedAreaHa,
        BigDecimal targetKg,
        BigDecimal realizedKg,
        BigDecimal pendingKg,
        long pendingExecutionsCount,
        LocalDate plantingDate,
        LocalDate expectedHarvestStart,
        LocalDate expectedHarvestEnd,
        LocalDate lastExecutionDate
) {
}
