package com.ufal.smartagro.adapters.in.web.dto.analytics;

import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.PlanProgressStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FarmerDashboardSummaryDTOTest {

    @Test
    @DisplayName("empty() deve instanciar DTO com valores padrão zerados e isEmpty true")
    void empty_instanciaValoresZeradosCorretamente() {
        UUID farmerId = UUID.randomUUID();
        HarvestSummaryInfoDTO harvest = new HarvestSummaryInfoDTO(
                UUID.randomUUID(),
                "Safra 2026",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );

        FarmerDashboardSummaryDTO dto = FarmerDashboardSummaryDTO.empty(
                farmerId,
                "Severino do Ramo",
                "Comunidade Esperança",
                harvest
        );

        assertThat(dto.farmerId()).isEqualTo(farmerId);
        assertThat(dto.farmerName()).isEqualTo("Severino do Ramo");
        assertThat(dto.communityName()).isEqualTo("Comunidade Esperança");
        assertThat(dto.currentHarvest()).isEqualTo(harvest);
        assertThat(dto.isEmpty()).isTrue();
        assertThat(dto.totalPlansCount()).isZero();
        assertThat(dto.activePlotsCount()).isZero();
        assertThat(dto.totalPlantedAreaHa()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.totalTargetKg()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.totalRealizedKg()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.totalPendingKg()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.overallCompletionPct()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.plans()).isEmpty();
    }

    @Test
    @DisplayName("Deve instanciar FarmerPlanSummaryCardDTO e FarmerDashboardSummaryDTO com dados completos")
    void construtorCompleto_populaCorretamente() {
        UUID planId = UUID.randomUUID();
        UUID cropId = UUID.randomUUID();

        FarmerPlanSummaryCardDTO card = new FarmerPlanSummaryCardDTO(
                planId,
                cropId,
                "Milho",
                "Híbrido",
                HarvestType.SINGLE,
                "KG",
                PlanStatus.ACTIVE,
                "Talhão Sul",
                new BigDecimal("2.50"),
                new BigDecimal("5000.00"),
                new BigDecimal("2500.00"),
                new BigDecimal("500.00"),
                new BigDecimal("50.00"),
                PlanProgressStatus.ON_TRACK,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 6, 15)
        );

        assertThat(card.planId()).isEqualTo(planId);
        assertThat(card.cropName()).isEqualTo("Milho");
        assertThat(card.progressStatus()).isEqualTo(PlanProgressStatus.ON_TRACK);
        assertThat(card.completionPct()).isEqualByComparingTo("50.00");

        FarmerDashboardSummaryDTO dto = new FarmerDashboardSummaryDTO(
                UUID.randomUUID(),
                "Maria",
                "Comunidade Centro",
                null,
                false,
                1,
                1,
                new BigDecimal("2.50"),
                new BigDecimal("5000.00"),
                new BigDecimal("2500.00"),
                new BigDecimal("500.00"),
                new BigDecimal("50.00"),
                List.of(card)
        );

        assertThat(dto.isEmpty()).isFalse();
        assertThat(dto.totalPlansCount()).isEqualTo(1);
        assertThat(dto.plans()).containsExactly(card);
    }
}
