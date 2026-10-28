package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestSummaryInfoDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianCommunitySummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianFarmerSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianPlanWarningDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropNotFoundException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.TechnicianAssistedFarmerRow;
import com.ufal.smartagro.domain.model.TechnicianPlanSummary;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.PlanProgressStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.model.enums.TechnicianWarningType;
import com.ufal.smartagro.domain.model.enums.WarningSeverity;
import com.ufal.smartagro.domain.port.out.CropRepository;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso para consolidar e retornar o dashboard agregado da carteira do técnico.
 */
@Service
@RequiredArgsConstructor
public class GetTechnicianDashboardSummaryUseCase {

    private final TechnicianRepository technicianRepository;
    private final TechnicalAssistanceRepository technicalAssistanceRepository;
    private final HarvestRepository harvestRepository;
    private final CropRepository cropRepository;
    private final ProductionPlanRepository productionPlanRepository;

    @Transactional(readOnly = true)
    public TechnicianDashboardSummaryDTO execute(
            UUID harvestId,
            UUID communityId,
            UUID cropId,
            User loggedUser
    ) {
        return execute(harvestId, communityId, cropId, loggedUser, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public TechnicianDashboardSummaryDTO execute(
            UUID harvestId,
            UUID communityId,
            UUID cropId,
            User loggedUser,
            LocalDate referenceDate
    ) {
        validateAccess(loggedUser);

        Technician technician = technicianRepository.findByUserId(loggedUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Perfil de técnico não encontrado para o usuário logado."));

        if (technician.getDeletedAt() != null) {
            throw new AccessDeniedException("Perfil de técnico inativo ou excluído.");
        }

        String technicianName = loggedUser.getFullName() != null && !loggedUser.getFullName().isBlank()
                ? loggedUser.getFullName()
                : (technician.getUser() != null && technician.getUser().getFullName() != null
                    ? technician.getUser().getFullName()
                    : "");

        // Validação de isolamento do técnico via technical_assistances
        if (communityId != null) {
            technicalAssistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), communityId)
                    .orElseThrow(() -> new AccessDeniedException("Comunidade não assistida pelo técnico logado."));
        }

        // Validação da cultura
        if (cropId != null) {
            cropRepository.findById(cropId)
                    .orElseThrow(() -> new CropNotFoundException("Cultura não encontrada."));
        }

        LocalDate today = referenceDate != null ? referenceDate : LocalDate.now();

        // Resolução da safra
        HarvestSummaryInfoDTO currentHarvestDto = null;
        UUID resolvedHarvestId = harvestId;
        boolean includeActiveContinuous = false;

        if (harvestId != null) {
            Harvest harvest = harvestRepository.findById(harvestId)
                    .orElseThrow(() -> new HarvestNotFoundException("Safra não encontrada."));
            currentHarvestDto = toHarvestSummaryInfoDTO(harvest);
            includeActiveContinuous = false;
        } else {
            Optional<Harvest> activeHarvest = harvestRepository.findActiveHarvest(today);
            if (activeHarvest.isPresent()) {
                Harvest harvest = activeHarvest.get();
                resolvedHarvestId = harvest.getId();
                currentHarvestDto = toHarvestSummaryInfoDTO(harvest);
                includeActiveContinuous = true;
            }
        }

        // Consulta 1: Carteira de assistência técnica (comunidades e agricultores ativos)
        List<TechnicianAssistedFarmerRow> assistedRows = productionPlanRepository
                .findAssistedFarmersByTechnician(technician.getId(), communityId);

        if (assistedRows.isEmpty()) {
            return TechnicianDashboardSummaryDTO.empty(
                    technician.getId(),
                    technicianName,
                    currentHarvestDto,
                    communityId,
                    cropId
            );
        }

        // Consulta 2: Planos agregados e apontamentos
        List<TechnicianPlanSummary> planSummaries = productionPlanRepository
                .findPlanSummariesByTechnician(technician.getId(), communityId, cropId, resolvedHarvestId, includeActiveContinuous);

        // Fallback gracioso: quando aberto sem safra explícita e a safra ativa ainda não possui planos,
        // busca planos em andamento (ACTIVE / PLANNED) para evitar falso Empty State
        if (harvestId == null && resolvedHarvestId != null && planSummaries.isEmpty()) {
            List<TechnicianPlanSummary> allPlans = productionPlanRepository
                    .findPlanSummariesByTechnician(technician.getId(), communityId, cropId, null, false);
            List<TechnicianPlanSummary> activeOrPlanned = allPlans.stream()
                    .filter(p -> p.status() == PlanStatus.ACTIVE || p.status() == PlanStatus.PLANNED)
                    .toList();
            if (!activeOrPlanned.isEmpty()) {
                planSummaries = activeOrPlanned;
            }
        }

        // Processar Motor de Alertas
        List<TechnicianPlanWarningDTO> warnings = evaluateWarnings(planSummaries, today);

        // Mapas para contagem rápida de alertas por plano, por comunidade e por agricultor
        Map<UUID, Long> warningsCountByCommunity = warnings.stream()
                .filter(w -> w.communityId() != null)
                .collect(Collectors.groupingBy(TechnicianPlanWarningDTO::communityId, Collectors.counting()));

        Map<UUID, Long> warningsCountByFarmer = warnings.stream()
                .filter(w -> w.farmerId() != null)
                .collect(Collectors.groupingBy(TechnicianPlanWarningDTO::farmerId, Collectors.counting()));

        // Agrupamento de planos por comunidade e por agricultor
        Map<UUID, List<TechnicianPlanSummary>> plansByCommunity = planSummaries.stream()
                .filter(p -> p.communityId() != null)
                .collect(Collectors.groupingBy(TechnicianPlanSummary::communityId));

        Map<UUID, List<TechnicianPlanSummary>> plansByFarmer = planSummaries.stream()
                .filter(p -> p.farmerId() != null)
                .collect(Collectors.groupingBy(TechnicianPlanSummary::farmerId));

        // Consolidar lista de Comunidades
        Map<UUID, String> distinctCommunities = new LinkedHashMap<>();
        for (TechnicianAssistedFarmerRow row : assistedRows) {
            if (row.communityId() != null) {
                distinctCommunities.putIfAbsent(row.communityId(), row.communityName());
            }
        }

        List<TechnicianCommunitySummaryDTO> communityDtos = new ArrayList<>(distinctCommunities.size());
        for (Map.Entry<UUID, String> entry : distinctCommunities.entrySet()) {
            UUID commId = entry.getKey();
            String commName = entry.getValue();

            // Total de agricultores assistidos nesta comunidade a partir de assistedRows
            long assistedFarmersCount = assistedRows.stream()
                    .filter(r -> commId.equals(r.communityId()) && r.farmerId() != null)
                    .map(TechnicianAssistedFarmerRow::farmerId)
                    .distinct()
                    .count();

            List<TechnicianPlanSummary> commPlans = plansByCommunity.getOrDefault(commId, List.of());
            long farmersWithPlansCount = commPlans.stream()
                    .map(TechnicianPlanSummary::farmerId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            BigDecimal plantedArea = BigDecimal.ZERO;
            BigDecimal targetKg = BigDecimal.ZERO;
            BigDecimal realizedKg = BigDecimal.ZERO;
            BigDecimal pendingKg = BigDecimal.ZERO;

            for (TechnicianPlanSummary plan : commPlans) {
                if (plan.plantedAreaHa() != null) plantedArea = plantedArea.add(plan.plantedAreaHa());
                if (plan.targetKg() != null) targetKg = targetKg.add(plan.targetKg());
                if (plan.realizedKg() != null) realizedKg = realizedKg.add(plan.realizedKg());
                if (plan.pendingKg() != null) pendingKg = pendingKg.add(plan.pendingKg());
            }

            BigDecimal completionPct = calculateCompletionPercentage(realizedKg, targetKg);
            int commWarningsCount = warningsCountByCommunity.getOrDefault(commId, 0L).intValue();

            communityDtos.add(new TechnicianCommunitySummaryDTO(
                    commId,
                    commName,
                    (int) assistedFarmersCount,
                    (int) farmersWithPlansCount,
                    commPlans.size(),
                    plantedArea.setScale(2, RoundingMode.HALF_UP),
                    targetKg.setScale(2, RoundingMode.HALF_UP),
                    realizedKg.setScale(2, RoundingMode.HALF_UP),
                    pendingKg.setScale(2, RoundingMode.HALF_UP),
                    completionPct.setScale(2, RoundingMode.HALF_UP),
                    commWarningsCount
            ));
        }
        communityDtos.sort(Comparator.comparing(TechnicianCommunitySummaryDTO::communityName, Comparator.nullsLast(String::compareToIgnoreCase)));

        // Consolidar lista de Agricultores Assistidos
        Map<UUID, TechnicianAssistedFarmerRow> distinctFarmers = new LinkedHashMap<>();
        for (TechnicianAssistedFarmerRow row : assistedRows) {
            if (row.farmerId() != null) {
                distinctFarmers.putIfAbsent(row.farmerId(), row);
            }
        }

        List<TechnicianFarmerSummaryDTO> farmerDtos = new ArrayList<>(distinctFarmers.size());
        for (TechnicianAssistedFarmerRow farmerRow : distinctFarmers.values()) {
            UUID fId = farmerRow.farmerId();
            List<TechnicianPlanSummary> fPlans = plansByFarmer.getOrDefault(fId, List.of());

            // Quando cropId está presente, exibe apenas os agricultores que possuem planos da cultura filtrada (Seção 3.3)
            if (cropId != null && fPlans.isEmpty()) {
                continue;
            }

            int activePlansCount = 0;
            BigDecimal plantedArea = BigDecimal.ZERO;
            BigDecimal targetKg = BigDecimal.ZERO;
            BigDecimal realizedKg = BigDecimal.ZERO;
            BigDecimal pendingKg = BigDecimal.ZERO;
            LocalDate lastExecutionDate = null;

            for (TechnicianPlanSummary plan : fPlans) {
                if (plan.status() == PlanStatus.ACTIVE) {
                    activePlansCount++;
                }
                if (plan.plantedAreaHa() != null) plantedArea = plantedArea.add(plan.plantedAreaHa());
                if (plan.targetKg() != null) targetKg = targetKg.add(plan.targetKg());
                if (plan.realizedKg() != null) realizedKg = realizedKg.add(plan.realizedKg());
                if (plan.pendingKg() != null) pendingKg = pendingKg.add(plan.pendingKg());

                if (plan.lastExecutionDate() != null) {
                    if (lastExecutionDate == null || plan.lastExecutionDate().isAfter(lastExecutionDate)) {
                        lastExecutionDate = plan.lastExecutionDate();
                    }
                }
            }

            BigDecimal completionPct = calculateCompletionPercentage(realizedKg, targetKg);
            int farmerWarningsCount = warningsCountByFarmer.getOrDefault(fId, 0L).intValue();
            boolean isCompliant = Boolean.TRUE.equals(farmerRow.isCompliant());

            farmerDtos.add(new TechnicianFarmerSummaryDTO(
                    fId,
                    farmerRow.farmerName(),
                    farmerRow.communityId(),
                    farmerRow.communityName(),
                    isCompliant,
                    fPlans.size(),
                    activePlansCount,
                    plantedArea.setScale(2, RoundingMode.HALF_UP),
                    targetKg.setScale(2, RoundingMode.HALF_UP),
                    realizedKg.setScale(2, RoundingMode.HALF_UP),
                    pendingKg.setScale(2, RoundingMode.HALF_UP),
                    completionPct.setScale(2, RoundingMode.HALF_UP),
                    lastExecutionDate,
                    farmerWarningsCount
            ));
        }
        farmerDtos.sort(Comparator.comparing(TechnicianFarmerSummaryDTO::farmerName, Comparator.nullsLast(String::compareToIgnoreCase)));

        // Totais Globais da Carteira
        int totalAssistedCommunitiesCount = distinctCommunities.size();
        int totalAssistedFarmersCount = distinctFarmers.size();
        int totalPlansCount = planSummaries.size();
        int activePlansCount = 0;
        BigDecimal totalPlantedArea = BigDecimal.ZERO;
        BigDecimal totalTargetKg = BigDecimal.ZERO;
        BigDecimal totalRealizedKg = BigDecimal.ZERO;
        BigDecimal totalPendingKg = BigDecimal.ZERO;
        int totalPendingExecutionsCount = 0;

        for (TechnicianPlanSummary plan : planSummaries) {
            if (plan.status() == PlanStatus.ACTIVE) {
                activePlansCount++;
            }
            if (plan.plantedAreaHa() != null) totalPlantedArea = totalPlantedArea.add(plan.plantedAreaHa());
            if (plan.targetKg() != null) totalTargetKg = totalTargetKg.add(plan.targetKg());
            if (plan.realizedKg() != null) totalRealizedKg = totalRealizedKg.add(plan.realizedKg());
            if (plan.pendingKg() != null) totalPendingKg = totalPendingKg.add(plan.pendingKg());
            totalPendingExecutionsCount += (int) plan.pendingExecutionsCount();
        }

        BigDecimal overallCompletionPct = calculateCompletionPercentage(totalRealizedKg, totalTargetKg);
        boolean isEmpty = planSummaries.isEmpty();

        return new TechnicianDashboardSummaryDTO(
                technician.getId(),
                technicianName,
                currentHarvestDto,
                communityId,
                cropId,
                isEmpty,
                totalAssistedCommunitiesCount,
                totalAssistedFarmersCount,
                totalPlansCount,
                activePlansCount,
                totalPlantedArea.setScale(2, RoundingMode.HALF_UP),
                totalTargetKg.setScale(2, RoundingMode.HALF_UP),
                totalRealizedKg.setScale(2, RoundingMode.HALF_UP),
                totalPendingKg.setScale(2, RoundingMode.HALF_UP),
                totalPendingExecutionsCount,
                overallCompletionPct.setScale(2, RoundingMode.HALF_UP),
                warnings.size(),
                warnings,
                communityDtos,
                farmerDtos
        );
    }

    private void validateAccess(User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("Acesso negado. Apenas técnicos podem acessar o resumo agregado.");
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

    /**
     * Avalia planos de produção e monta a lista desduplicada de alertas do técnico.
     */
    private List<TechnicianPlanWarningDTO> evaluateWarnings(List<TechnicianPlanSummary> planSummaries, LocalDate today) {
        List<TechnicianPlanWarningDTO> warnings = new ArrayList<>();

        for (TechnicianPlanSummary summary : planSummaries) {
            // Apenas planos não cancelados e em andamento (ACTIVE ou PLANNED)
            if (summary.status() != PlanStatus.ACTIVE && summary.status() != PlanStatus.PLANNED) {
                continue;
            }

            BigDecimal targetKg = summary.targetKg() != null ? summary.targetKg() : BigDecimal.ZERO;
            BigDecimal realizedKg = summary.realizedKg() != null ? summary.realizedKg() : BigDecimal.ZERO;
            BigDecimal pendingKg = summary.pendingKg() != null ? summary.pendingKg() : BigDecimal.ZERO;
            BigDecimal completionPct = calculateCompletionPercentage(realizedKg, targetKg);

            // Planos que atingiram 100% da meta nunca geram alerta
            if (completionPct.compareTo(BigDecimal.valueOf(100)) >= 0) {
                continue;
            }

            List<TechnicianWarningType> triggeredTypes = new ArrayList<>();
            List<String> messages = new ArrayList<>();
            boolean isHighSeverity = false;

            Integer daysWithoutExecution = null;
            BigDecimal calculatedExpectedPaceKg = null;

            // 1. Alerta: INACTIVE_EXECUTIONS (> 15 dias sem apontamento)
            boolean isActiveOrStartingPlanned = summary.status() == PlanStatus.ACTIVE
                    || (summary.status() == PlanStatus.PLANNED
                        && summary.expectedHarvestStart() != null
                        && !today.isBefore(summary.expectedHarvestStart()));

            if (isActiveOrStartingPlanned) {
                if (summary.lastExecutionDate() != null) {
                    long days = ChronoUnit.DAYS.between(summary.lastExecutionDate(), today);
                    if (days > 15) {
                        triggeredTypes.add(TechnicianWarningType.INACTIVE_EXECUTIONS);
                        daysWithoutExecution = (int) days;
                        if (days > 30) {
                            isHighSeverity = true;
                        }
                        messages.add("Sem apontamentos há " + daysWithoutExecution + " dias");
                    }
                } else {
                    LocalDate baseDate = summary.expectedHarvestStart() != null
                            ? summary.expectedHarvestStart()
                            : summary.plantingDate();
                    if (baseDate != null) {
                        long days = ChronoUnit.DAYS.between(baseDate, today);
                        if (days > 15) {
                            triggeredTypes.add(TechnicianWarningType.INACTIVE_EXECUTIONS);
                            daysWithoutExecution = (int) days;
                            if (days > 30) {
                                isHighSeverity = true;
                            }
                            messages.add("Sem apontamentos há " + daysWithoutExecution + " dias");
                        }
                    }
                }
            }

            // 2. Alerta: LOW_YIELD_RISK (Mais de 30% abaixo da curva linear esperada)
            LocalDate harvestStart = summary.expectedHarvestStart();
            LocalDate harvestEnd = summary.expectedHarvestEnd();

            if (harvestStart != null && harvestEnd != null && !harvestStart.isAfter(harvestEnd)) {
                if (!today.isBefore(harvestStart) && !today.isAfter(harvestEnd)) {
                    long totalDays = ChronoUnit.DAYS.between(harvestStart, harvestEnd) + 1;
                    long elapsedDays = ChronoUnit.DAYS.between(harvestStart, today) + 1;
                    elapsedDays = Math.min(elapsedDays, totalDays);

                    if (totalDays > 0 && targetKg.compareTo(BigDecimal.ZERO) > 0 && elapsedDays > 0) {
                        BigDecimal expectedPaceKg = targetKg
                                .multiply(BigDecimal.valueOf(elapsedDays))
                                .divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP);
                        calculatedExpectedPaceKg = expectedPaceKg;

                        BigDecimal threshold70Pct = expectedPaceKg.multiply(BigDecimal.valueOf(0.70));
                        if (realizedKg.compareTo(threshold70Pct) < 0) {
                            triggeredTypes.add(TechnicianWarningType.LOW_YIELD_RISK);

                            BigDecimal threshold50Pct = expectedPaceKg.multiply(BigDecimal.valueOf(0.50));
                            if (realizedKg.compareTo(threshold50Pct) < 0) {
                                isHighSeverity = true;
                            }

                            BigDecimal deficitKg = expectedPaceKg.subtract(realizedKg);
                            BigDecimal deficitPct = deficitKg.multiply(BigDecimal.valueOf(100))
                                    .divide(expectedPaceKg, 1, RoundingMode.HALF_UP);

                            messages.add(String.format(
                                    Locale.forLanguageTag("pt-BR"),
                                    "Produção %.1f%% abaixo do esperado para o período",
                                    deficitPct
                            ));
                        }
                    }
                }
            }

            // 3. Alerta: HARVEST_OVERDUE (Fim de ciclo ultrapassado sem atingir a meta)
            if (harvestEnd != null && today.isAfter(harvestEnd)) {
                triggeredTypes.add(TechnicianWarningType.HARVEST_OVERDUE);
                isHighSeverity = true;
                long daysOverdue = ChronoUnit.DAYS.between(harvestEnd, today);
                messages.add("Janela de colheita encerrada há " + daysOverdue + (daysOverdue == 1 ? " dia" : " dias"));
            }

            // Se algum alerta foi disparado para este plano, consolida em um único cartão
            if (!triggeredTypes.isEmpty()) {
                WarningSeverity severity = (isHighSeverity || triggeredTypes.size() > 1)
                        ? WarningSeverity.HIGH
                        : WarningSeverity.MEDIUM;
                String combinedMessage = String.join(" • ", messages);
                PlanProgressStatus progressStatus = determineProgressStatus(summary, completionPct, realizedKg, targetKg, today);

                warnings.add(new TechnicianPlanWarningDTO(
                        summary.planId(),
                        summary.farmerId(),
                        summary.farmerName(),
                        summary.communityId(),
                        summary.communityName(),
                        summary.cropId(),
                        summary.cropName(),
                        summary.cropVariety(),
                        summary.status(),
                        progressStatus,
                        triggeredTypes,
                        severity,
                        combinedMessage,
                        targetKg.setScale(2, RoundingMode.HALF_UP),
                        realizedKg.setScale(2, RoundingMode.HALF_UP),
                        pendingKg.setScale(2, RoundingMode.HALF_UP),
                        calculatedExpectedPaceKg != null ? calculatedExpectedPaceKg.setScale(2, RoundingMode.HALF_UP) : null,
                        completionPct.setScale(2, RoundingMode.HALF_UP),
                        summary.lastExecutionDate(),
                        daysWithoutExecution,
                        summary.expectedHarvestStart(),
                        summary.expectedHarvestEnd()
                ));
            }
        }

        // Ordenação: HIGH primeiro, depois menor completionPct
        warnings.sort(Comparator
                .comparing(TechnicianPlanWarningDTO::severity, (s1, s2) -> {
                    if (s1 == s2) return 0;
                    return s1 == WarningSeverity.HIGH ? -1 : 1;
                })
                .thenComparing(TechnicianPlanWarningDTO::completionPct)
        );

        return warnings;
    }

    PlanProgressStatus determineProgressStatus(
            TechnicianPlanSummary summary,
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
