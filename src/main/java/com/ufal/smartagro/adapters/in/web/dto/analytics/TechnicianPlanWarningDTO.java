package com.ufal.smartagro.adapters.in.web.dto.analytics;

import com.ufal.smartagro.domain.model.enums.PlanProgressStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.model.enums.TechnicianWarningType;
import com.ufal.smartagro.domain.model.enums.WarningSeverity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Cartão de alerta operacional acionável para o painel do técnico.
 */
public record TechnicianPlanWarningDTO(
        UUID planId,
        UUID farmerId,
        String farmerName,
        UUID communityId,
        String communityName,
        UUID cropId,
        String cropName,
        String cropVariety,
        PlanStatus planStatus,
        PlanProgressStatus progressStatus,
        List<TechnicianWarningType> warningTypes,
        WarningSeverity severity,
        String message,
        BigDecimal targetKg,
        BigDecimal realizedKg,
        BigDecimal pendingKg,
        BigDecimal expectedPaceKg,
        BigDecimal completionPct,
        LocalDate lastExecutionDate,
        Integer daysWithoutExecution,
        LocalDate expectedHarvestStart,
        LocalDate expectedHarvestEnd
) {
}
