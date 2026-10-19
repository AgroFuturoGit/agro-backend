package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerPlanSummaryCardDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestSummaryInfoDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.FarmerPlanSummary;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.PlanProgressStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para consolidar e retornar o dashboard individual do agricultor autenticado.
 */
@Service
@RequiredArgsConstructor
public class GetFarmerDashboardSummaryUseCase {

    private final FarmerRepository farmerRepository;
    private final HarvestRepository harvestRepository;
    private final ProductionPlanRepository productionPlanRepository;

    @Transactional(readOnly = true)
    public FarmerDashboardSummaryDTO execute(UUID harvestId, User loggedUser) {
        return execute(harvestId, loggedUser, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public FarmerDashboardSummaryDTO execute(UUID harvestId, User loggedUser, LocalDate referenceDate) {
        validateAccess(loggedUser);

        Farmer farmer = farmerRepository.findByUserId(loggedUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Perfil de agricultor não encontrado para o usuário logado."));

        if (farmer.getDeletedAt() != null) {
            throw new AccessDeniedException("Perfil de agricultor inativo ou excluído.");
        }

        LocalDate today = referenceDate != null ? referenceDate : LocalDate.now();

        // Resolução da safra
        HarvestSummaryInfoDTO currentHarvestDto = null;
        UUID resolvedHarvestId = harvestId;

        if (harvestId != null) {
            Harvest harvest = harvestRepository.findById(harvestId)
                    .orElseThrow(() -> new HarvestNotFoundException("Safra não encontrada."));
            currentHarvestDto = toHarvestSummaryInfoDTO(harvest);
        } else {
            Optional<Harvest> activeHarvest = harvestRepository.findActiveHarvest(today);
            if (activeHarvest.isPresent()) {
                Harvest harvest = activeHarvest.get();
                resolvedHarvestId = harvest.getId();
                currentHarvestDto = toHarvestSummaryInfoDTO(harvest);
            }
        }

        String farmerName = farmer.getFullName() != null && !farmer.getFullName().isBlank()
                ? farmer.getFullName()
                : (loggedUser.getFullName() != null ? loggedUser.getFullName() : "");
        String communityName = farmer.getCommunity() != null ? farmer.getCommunity().getName() : null;

        List<FarmerPlanSummary> planSummaries = productionPlanRepository
                .findPlanSummariesByFarmer(farmer.getId(), resolvedHarvestId);

        if (planSummaries.isEmpty()) {
            return FarmerDashboardSummaryDTO.empty(farmer.getId(), farmerName, communityName, currentHarvestDto);
        }

        int activePlotsCount = 0;
        BigDecimal totalPlantedArea = BigDecimal.ZERO;
        BigDecimal totalTargetKg = BigDecimal.ZERO;
        BigDecimal totalRealizedKg = BigDecimal.ZERO;
        BigDecimal totalPendingKg = BigDecimal.ZERO;

        List<FarmerPlanSummaryCardDTO> cardDtos = new ArrayList<>(planSummaries.size());

        for (FarmerPlanSummary summary : planSummaries) {
            BigDecimal plantedArea = summary.plantedAreaHa() != null ? summary.plantedAreaHa() : BigDecimal.ZERO;
            BigDecimal targetKg = summary.targetKg() != null ? summary.targetKg() : BigDecimal.ZERO;
            BigDecimal realizedKg = summary.realizedKg() != null ? summary.realizedKg() : BigDecimal.ZERO;
            BigDecimal pendingKg = summary.pendingKg() != null ? summary.pendingKg() : BigDecimal.ZERO;

            BigDecimal completionPct = calculateCompletionPercentage(realizedKg, targetKg);
            PlanProgressStatus progressStatus = determineProgressStatus(summary, completionPct, realizedKg, targetKg, today);

            if (summary.status() == PlanStatus.ACTIVE || summary.status() == PlanStatus.PLANNED) {
                activePlotsCount++;
            }

            totalPlantedArea = totalPlantedArea.add(plantedArea);
            totalTargetKg = totalTargetKg.add(targetKg);
            totalRealizedKg = totalRealizedKg.add(realizedKg);
            totalPendingKg = totalPendingKg.add(pendingKg);

            cardDtos.add(new FarmerPlanSummaryCardDTO(
                    summary.planId(),
                    summary.cropId(),
                    summary.cropName(),
                    summary.cropVariety(),
                    summary.harvestType(),
                    summary.unit(),
                    summary.status(),
                    summary.locationDescription(),
                    plantedArea.setScale(2, RoundingMode.HALF_UP),
                    targetKg.setScale(2, RoundingMode.HALF_UP),
                    realizedKg.setScale(2, RoundingMode.HALF_UP),
                    pendingKg.setScale(2, RoundingMode.HALF_UP),
                    completionPct.setScale(2, RoundingMode.HALF_UP),
                    progressStatus,
                    summary.plantingDate(),
                    summary.expectedHarvestStart(),
                    summary.expectedHarvestEnd(),
                    summary.lastExecutionDate()
            ));
        }

        BigDecimal overallCompletionPct = calculateCompletionPercentage(totalRealizedKg, totalTargetKg);

        return new FarmerDashboardSummaryDTO(
                farmer.getId(),
                farmerName,
                communityName,
                currentHarvestDto,
                false,
                cardDtos.size(),
                activePlotsCount,
                totalPlantedArea.setScale(2, RoundingMode.HALF_UP),
                totalTargetKg.setScale(2, RoundingMode.HALF_UP),
                totalRealizedKg.setScale(2, RoundingMode.HALF_UP),
                totalPendingKg.setScale(2, RoundingMode.HALF_UP),
                overallCompletionPct.setScale(2, RoundingMode.HALF_UP),
                cardDtos
        );
    }

    private void validateAccess(User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.FARMER) {
            throw new AccessDeniedException("Acesso negado. Apenas agricultores podem acessar o resumo individual.");
        }
    }

    private HarvestSummaryInfoDTO toHarvestSummaryInfoDTO(Harvest harvest) {
        return new HarvestSummaryInfoDTO(
                harvest.getId(),
                harvest.getLabel(),
                harvest.getStartDate(),
                harvest.getEndDate()
        );
    }

    private BigDecimal calculateCompletionPercentage(BigDecimal realized, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return realized.multiply(BigDecimal.valueOf(100))
                .divide(target, 2, RoundingMode.HALF_UP);
    }

    PlanProgressStatus determineProgressStatus(
            FarmerPlanSummary summary,
            BigDecimal completionPct,
            BigDecimal realizedKg,
            BigDecimal targetKg,
            LocalDate referenceDate
    ) {
        if (completionPct.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return PlanProgressStatus.GOAL_REACHED;
        }

        LocalDate harvestEnd = summary.expectedHarvestEnd();
        LocalDate harvestStart = summary.expectedHarvestStart();

        if (harvestEnd != null && referenceDate.isAfter(harvestEnd)) {
            return PlanProgressStatus.DELAYED;
        }

        boolean notStartedByStatus = summary.status() == PlanStatus.PLANNED;
        boolean notStartedByDate = harvestStart != null && referenceDate.isBefore(harvestStart);
        boolean zeroRealized = realizedKg.compareTo(BigDecimal.ZERO) == 0;

        if (zeroRealized && (notStartedByStatus || notStartedByDate)) {
            return PlanProgressStatus.NOT_STARTED;
        }

        if (harvestStart != null && harvestEnd != null && !harvestStart.isAfter(harvestEnd)) {
            if (!referenceDate.isBefore(harvestStart)) {
                long totalDays = ChronoUnit.DAYS.between(harvestStart, harvestEnd) + 1;
                long elapsedDays = ChronoUnit.DAYS.between(harvestStart, referenceDate) + 1;
                elapsedDays = Math.min(elapsedDays, totalDays);

                if (totalDays > 0 && targetKg.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal expectedPaceKg = targetKg
                            .multiply(BigDecimal.valueOf(elapsedDays))
                            .divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP);

                    BigDecimal threshold70Pct = expectedPaceKg.multiply(BigDecimal.valueOf(0.70));
                    if (realizedKg.compareTo(threshold70Pct) < 0) {
                        return PlanProgressStatus.BELOW_EXPECTED;
                    }
                }
            }
        }

        return PlanProgressStatus.ON_TRACK;
    }
}
