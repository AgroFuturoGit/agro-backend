package com.ufal.smartagro.adapters.in.web.dto.analytics;

import com.ufal.smartagro.domain.model.enums.ManagementDashboardScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ManagementDashboardSummaryDTOTest {

    @Test
    @DisplayName("empty() deve instanciar DTO com valores padrão zerados e isEmpty true")
    void empty_instanciaValoresZeradosCorretamente() {
        UUID orgId = UUID.randomUUID();
        UUID commId = UUID.randomUUID();
        HarvestSummaryInfoDTO harvest = new HarvestSummaryInfoDTO(
                UUID.randomUUID(), "Safra 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)
        );

        ManagementDashboardSummaryDTO dto = ManagementDashboardSummaryDTO.empty(
                ManagementDashboardScope.ORGANIZATION,
                orgId,
                "COOPAFAL",
                harvest,
                commId
        );

        assertThat(dto.scope()).isEqualTo(ManagementDashboardScope.ORGANIZATION);
        assertThat(dto.organizationId()).isEqualTo(orgId);
        assertThat(dto.organizationName()).isEqualTo("COOPAFAL");
        assertThat(dto.currentHarvest()).isEqualTo(harvest);
        assertThat(dto.appliedCommunityId()).isEqualTo(commId);
        assertThat(dto.isEmpty()).isTrue();
        assertThat(dto.totalOrganizationsCount()).isZero();
        assertThat(dto.totalCommunitiesCount()).isZero();
        assertThat(dto.totalFarmersCount()).isZero();
        assertThat(dto.compliantFarmersCount()).isZero();
        assertThat(dto.complianceRatePct()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.totalPlansCount()).isZero();
        assertThat(dto.totalPlantedAreaHa()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.totalTargetKg()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.totalRealizedKg()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.overallConversionRatePct()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.cropRanking()).isEmpty();
        assertThat(dto.communities()).isEmpty();
        assertThat(dto.organizations()).isEmpty();
    }

    @Test
    @DisplayName("Deve instanciar DTOs gerenciais com dados completos")
    void construtorCompleto_populaCorretamente() {
        UUID orgId = UUID.randomUUID();
        UUID commId = UUID.randomUUID();
        UUID cropId = UUID.randomUUID();

        ManagementCropRankingItemDTO cropItem = new ManagementCropRankingItemDTO(
                1,
                cropId,
                "Mandioca",
                "Branca",
                "KG",
                30,
                new BigDecimal("70.00"),
                new BigDecimal("140000.00"),
                new BigDecimal("120000.00"),
                new BigDecimal("85.71"),
                new BigDecimal("64.52")
        );

        assertThat(cropItem.rankPosition()).isEqualTo(1);
        assertThat(cropItem.cropId()).isEqualTo(cropId);
        assertThat(cropItem.cropName()).isEqualTo("Mandioca");
        assertThat(cropItem.cropVariety()).isEqualTo("Branca");
        assertThat(cropItem.unit()).isEqualTo("KG");
        assertThat(cropItem.plansCount()).isEqualTo(30);
        assertThat(cropItem.plantedAreaHa()).isEqualByComparingTo("70.00");
        assertThat(cropItem.targetKg()).isEqualByComparingTo("140000.00");
        assertThat(cropItem.realizedKg()).isEqualByComparingTo("120000.00");
        assertThat(cropItem.conversionRatePct()).isEqualByComparingTo("85.71");
        assertThat(cropItem.productionSharePct()).isEqualByComparingTo("64.52");

        ManagementCommunityPerformanceDTO commPerformance = new ManagementCommunityPerformanceDTO(
                commId,
                "Comunidade São José",
                orgId,
                "COOPAFAL",
                18,
                17,
                new BigDecimal("94.44"),
                25,
                new BigDecimal("55.00"),
                new BigDecimal("110000.00"),
                new BigDecimal("95000.00"),
                new BigDecimal("86.36"),
                LocalDate.of(2026, 10, 18)
        );

        assertThat(commPerformance.communityId()).isEqualTo(commId);
        assertThat(commPerformance.communityName()).isEqualTo("Comunidade São José");
        assertThat(commPerformance.organizationId()).isEqualTo(orgId);
        assertThat(commPerformance.organizationName()).isEqualTo("COOPAFAL");
        assertThat(commPerformance.activeFarmersCount()).isEqualTo(18);
        assertThat(commPerformance.compliantFarmersCount()).isEqualTo(17);
        assertThat(commPerformance.complianceRatePct()).isEqualByComparingTo("94.44");
        assertThat(commPerformance.plansCount()).isEqualTo(25);
        assertThat(commPerformance.plantedAreaHa()).isEqualByComparingTo("55.00");
        assertThat(commPerformance.targetKg()).isEqualByComparingTo("110000.00");
        assertThat(commPerformance.realizedKg()).isEqualByComparingTo("95000.00");
        assertThat(commPerformance.conversionRatePct()).isEqualByComparingTo("86.36");
        assertThat(commPerformance.lastExecutionDate()).isEqualTo(LocalDate.of(2026, 10, 18));

        ManagementOrganizationSummaryDTO orgSummary = new ManagementOrganizationSummaryDTO(
                orgId,
                "COOPAFAL",
                3,
                42,
                38,
                new BigDecimal("90.48"),
                55,
                new BigDecimal("120.50"),
                new BigDecimal("240000.00"),
                new BigDecimal("186000.00"),
                new BigDecimal("77.50")
        );

        assertThat(orgSummary.organizationId()).isEqualTo(orgId);
        assertThat(orgSummary.organizationName()).isEqualTo("COOPAFAL");
        assertThat(orgSummary.communitiesCount()).isEqualTo(3);
        assertThat(orgSummary.activeFarmersCount()).isEqualTo(42);
        assertThat(orgSummary.compliantFarmersCount()).isEqualTo(38);
        assertThat(orgSummary.complianceRatePct()).isEqualByComparingTo("90.48");
        assertThat(orgSummary.plansCount()).isEqualTo(55);
        assertThat(orgSummary.plantedAreaHa()).isEqualByComparingTo("120.50");
        assertThat(orgSummary.targetKg()).isEqualByComparingTo("240000.00");
        assertThat(orgSummary.realizedKg()).isEqualByComparingTo("186000.00");
        assertThat(orgSummary.conversionRatePct()).isEqualByComparingTo("77.50");

        HarvestSummaryInfoDTO harvest = new HarvestSummaryInfoDTO(
                UUID.randomUUID(), "Safra 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)
        );

        ManagementDashboardSummaryDTO dto = new ManagementDashboardSummaryDTO(
                ManagementDashboardScope.ORGANIZATION,
                orgId,
                "COOPAFAL",
                harvest,
                null,
                false,
                1,
                3,
                42,
                38,
                new BigDecimal("90.48"),
                55,
                new BigDecimal("120.50"),
                new BigDecimal("240000.00"),
                new BigDecimal("186000.00"),
                new BigDecimal("77.50"),
                List.of(cropItem),
                List.of(commPerformance),
                List.of(orgSummary)
        );

        assertThat(dto.scope()).isEqualTo(ManagementDashboardScope.ORGANIZATION);
        assertThat(dto.isEmpty()).isFalse();
        assertThat(dto.totalOrganizationsCount()).isEqualTo(1);
        assertThat(dto.totalCommunitiesCount()).isEqualTo(3);
        assertThat(dto.totalFarmersCount()).isEqualTo(42);
        assertThat(dto.compliantFarmersCount()).isEqualTo(38);
        assertThat(dto.complianceRatePct()).isEqualByComparingTo("90.48");
        assertThat(dto.totalPlansCount()).isEqualTo(55);
        assertThat(dto.totalPlantedAreaHa()).isEqualByComparingTo("120.50");
        assertThat(dto.totalTargetKg()).isEqualByComparingTo("240000.00");
        assertThat(dto.totalRealizedKg()).isEqualByComparingTo("186000.00");
        assertThat(dto.overallConversionRatePct()).isEqualByComparingTo("77.50");
        assertThat(dto.cropRanking()).containsExactly(cropItem);
        assertThat(dto.communities()).containsExactly(commPerformance);
        assertThat(dto.organizations()).containsExactly(orgSummary);
    }
}
