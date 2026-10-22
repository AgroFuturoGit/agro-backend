package com.ufal.smartagro.adapters.in.web.dto.analytics;

import com.ufal.smartagro.domain.model.enums.PlanProgressStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.model.enums.TechnicianWarningType;
import com.ufal.smartagro.domain.model.enums.WarningSeverity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TechnicianDashboardSummaryDTOTest {

    @Test
    @DisplayName("empty() deve instanciar DTO com valores padrão zerados e isEmpty true")
    void empty_instanciaValoresZeradosCorretamente() {
        UUID technicianId = UUID.randomUUID();
        UUID appliedCommunityId = UUID.randomUUID();
        UUID appliedCropId = UUID.randomUUID();

        HarvestSummaryInfoDTO harvest = new HarvestSummaryInfoDTO(
                UUID.randomUUID(),
                "Safra 2026",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );

        TechnicianDashboardSummaryDTO dto = TechnicianDashboardSummaryDTO.empty(
                technicianId,
                "Carlos Técnico",
                harvest,
                appliedCommunityId,
                appliedCropId
        );

        assertThat(dto.technicianId()).isEqualTo(technicianId);
        assertThat(dto.technicianName()).isEqualTo("Carlos Técnico");
        assertThat(dto.currentHarvest()).isEqualTo(harvest);
        assertThat(dto.appliedCommunityId()).isEqualTo(appliedCommunityId);
        assertThat(dto.appliedCropId()).isEqualTo(appliedCropId);
        assertThat(dto.isEmpty()).isTrue();
        assertThat(dto.totalAssistedCommunitiesCount()).isZero();
        assertThat(dto.totalAssistedFarmersCount()).isZero();
        assertThat(dto.totalPlansCount()).isZero();
        assertThat(dto.activePlansCount()).isZero();
        assertThat(dto.totalPlantedAreaHa()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.totalTargetKg()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.totalRealizedKg()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.totalPendingKg()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.pendingExecutionsCount()).isZero();
        assertThat(dto.overallCompletionPct()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.totalWarningsCount()).isZero();
        assertThat(dto.warnings()).isEmpty();
        assertThat(dto.communities()).isEmpty();
        assertThat(dto.farmers()).isEmpty();
    }

    @Test
    @DisplayName("Deve instanciar DTOs do dashboard do técnico com dados completos")
    void construtorCompleto_populaCorretamente() {
        UUID planId = UUID.randomUUID();
        UUID farmerId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        UUID cropId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();

        TechnicianPlanWarningDTO warning = new TechnicianPlanWarningDTO(
                planId,
                farmerId,
                "João da Silva",
                communityId,
                "Comunidade São José",
                cropId,
                "Tomate",
                "Cereja",
                PlanStatus.ACTIVE,
                PlanProgressStatus.BELOW_EXPECTED,
                List.of(TechnicianWarningType.INACTIVE_EXECUTIONS, TechnicianWarningType.LOW_YIELD_RISK),
                WarningSeverity.HIGH,
                "Sem apontamentos há 19 dias • Produção 42,0% abaixo do esperado para o período",
                new BigDecimal("5000.00"),
                new BigDecimal("1450.00"),
                new BigDecimal("200.00"),
                new BigDecimal("2500.00"),
                new BigDecimal("29.00"),
                LocalDate.of(2026, 10, 2),
                19,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 11, 30)
        );

        assertThat(warning.planId()).isEqualTo(planId);
        assertThat(warning.farmerId()).isEqualTo(farmerId);
        assertThat(warning.farmerName()).isEqualTo("João da Silva");
        assertThat(warning.communityId()).isEqualTo(communityId);
        assertThat(warning.communityName()).isEqualTo("Comunidade São José");
        assertThat(warning.cropId()).isEqualTo(cropId);
        assertThat(warning.cropName()).isEqualTo("Tomate");
        assertThat(warning.cropVariety()).isEqualTo("Cereja");
        assertThat(warning.planStatus()).isEqualTo(PlanStatus.ACTIVE);
        assertThat(warning.progressStatus()).isEqualTo(PlanProgressStatus.BELOW_EXPECTED);
        assertThat(warning.warningTypes()).containsExactly(
                TechnicianWarningType.INACTIVE_EXECUTIONS,
                TechnicianWarningType.LOW_YIELD_RISK
        );
        assertThat(warning.severity()).isEqualTo(WarningSeverity.HIGH);
        assertThat(warning.message()).contains("Sem apontamentos");
        assertThat(warning.targetKg()).isEqualByComparingTo("5000.00");
        assertThat(warning.realizedKg()).isEqualByComparingTo("1450.00");
        assertThat(warning.pendingKg()).isEqualByComparingTo("200.00");
        assertThat(warning.expectedPaceKg()).isEqualByComparingTo("2500.00");
        assertThat(warning.completionPct()).isEqualByComparingTo("29.00");
        assertThat(warning.lastExecutionDate()).isEqualTo(LocalDate.of(2026, 10, 2));
        assertThat(warning.daysWithoutExecution()).isEqualTo(19);
        assertThat(warning.expectedHarvestStart()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(warning.expectedHarvestEnd()).isEqualTo(LocalDate.of(2026, 11, 30));

        TechnicianCommunitySummaryDTO communitySummary = new TechnicianCommunitySummaryDTO(
                communityId,
                "Comunidade São José",
                8,
                7,
                10,
                new BigDecimal("18.00"),
                new BigDecimal("36000.00"),
                new BigDecimal("25200.00"),
                new BigDecimal("1800.00"),
                new BigDecimal("70.00"),
                1
        );

        assertThat(communitySummary.communityId()).isEqualTo(communityId);
        assertThat(communitySummary.communityName()).isEqualTo("Comunidade São José");
        assertThat(communitySummary.assistedFarmersCount()).isEqualTo(8);
        assertThat(communitySummary.farmersWithPlansCount()).isEqualTo(7);
        assertThat(communitySummary.plansCount()).isEqualTo(10);
        assertThat(communitySummary.plantedAreaHa()).isEqualByComparingTo("18.00");
        assertThat(communitySummary.targetKg()).isEqualByComparingTo("36000.00");
        assertThat(communitySummary.realizedKg()).isEqualByComparingTo("25200.00");
        assertThat(communitySummary.pendingKg()).isEqualByComparingTo("1800.00");
        assertThat(communitySummary.completionPct()).isEqualByComparingTo("70.00");
        assertThat(communitySummary.warningsCount()).isEqualTo(1);

        TechnicianFarmerSummaryDTO farmerSummary = new TechnicianFarmerSummaryDTO(
                farmerId,
                "João da Silva",
                communityId,
                "Comunidade São José",
                true,
                2,
                2,
                new BigDecimal("4.50"),
                new BigDecimal("9000.00"),
                new BigDecimal("6450.00"),
                new BigDecimal("200.00"),
                new BigDecimal("71.67"),
                LocalDate.of(2026, 10, 2),
                1
        );

        assertThat(farmerSummary.farmerId()).isEqualTo(farmerId);
        assertThat(farmerSummary.farmerName()).isEqualTo("João da Silva");
        assertThat(farmerSummary.communityId()).isEqualTo(communityId);
        assertThat(farmerSummary.communityName()).isEqualTo("Comunidade São José");
        assertThat(farmerSummary.isCompliant()).isTrue();
        assertThat(farmerSummary.plansCount()).isEqualTo(2);
        assertThat(farmerSummary.activePlansCount()).isEqualTo(2);
        assertThat(farmerSummary.plantedAreaHa()).isEqualByComparingTo("4.50");
        assertThat(farmerSummary.targetKg()).isEqualByComparingTo("9000.00");
        assertThat(farmerSummary.realizedKg()).isEqualByComparingTo("6450.00");
        assertThat(farmerSummary.pendingKg()).isEqualByComparingTo("200.00");
        assertThat(farmerSummary.completionPct()).isEqualByComparingTo("71.67");
        assertThat(farmerSummary.lastExecutionDate()).isEqualTo(LocalDate.of(2026, 10, 2));
        assertThat(farmerSummary.warningsCount()).isEqualTo(1);

        HarvestSummaryInfoDTO harvest = new HarvestSummaryInfoDTO(
                UUID.randomUUID(),
                "Safra 2026",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );

        TechnicianDashboardSummaryDTO dto = new TechnicianDashboardSummaryDTO(
                technicianId,
                "Carlos Eduardo Mendes",
                harvest,
                null,
                null,
                false,
                1,
                1,
                2,
                2,
                new BigDecimal("4.50"),
                new BigDecimal("9000.00"),
                new BigDecimal("6450.00"),
                new BigDecimal("200.00"),
                1,
                new BigDecimal("71.67"),
                1,
                List.of(warning),
                List.of(communitySummary),
                List.of(farmerSummary)
        );

        assertThat(dto.isEmpty()).isFalse();
        assertThat(dto.technicianId()).isEqualTo(technicianId);
        assertThat(dto.technicianName()).isEqualTo("Carlos Eduardo Mendes");
        assertThat(dto.currentHarvest()).isEqualTo(harvest);
        assertThat(dto.appliedCommunityId()).isNull();
        assertThat(dto.appliedCropId()).isNull();
        assertThat(dto.totalAssistedCommunitiesCount()).isEqualTo(1);
        assertThat(dto.totalAssistedFarmersCount()).isEqualTo(1);
        assertThat(dto.totalPlansCount()).isEqualTo(2);
        assertThat(dto.activePlansCount()).isEqualTo(2);
        assertThat(dto.totalPlantedAreaHa()).isEqualByComparingTo("4.50");
        assertThat(dto.totalTargetKg()).isEqualByComparingTo("9000.00");
        assertThat(dto.totalRealizedKg()).isEqualByComparingTo("6450.00");
        assertThat(dto.totalPendingKg()).isEqualByComparingTo("200.00");
        assertThat(dto.pendingExecutionsCount()).isEqualTo(1);
        assertThat(dto.overallCompletionPct()).isEqualByComparingTo("71.67");
        assertThat(dto.totalWarningsCount()).isEqualTo(1);
        assertThat(dto.warnings()).containsExactly(warning);
        assertThat(dto.communities()).containsExactly(communitySummary);
        assertThat(dto.farmers()).containsExactly(farmerSummary);
    }
}
