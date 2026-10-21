package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.ProductionPlanEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.ProductionPlanMapper;
import com.ufal.smartagro.adapters.out.persistence.projection.FarmerPlanSummaryProjection;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaProductionPlanRepository;
import com.ufal.smartagro.domain.model.FarmerPlanSummary;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionPlanRepositoryImplTest {

    @Mock
    private JpaProductionPlanRepository jpaRepository;

    @Mock
    private ProductionPlanMapper mapper;

    @InjectMocks
    private ProductionPlanRepositoryImpl repository;

    private UUID farmerId;
    private UUID harvestId;
    private UUID planId;
    private UUID cropId;

    @BeforeEach
    void setUp() {
        farmerId = UUID.randomUUID();
        harvestId = UUID.randomUUID();
        planId = UUID.randomUUID();
        cropId = UUID.randomUUID();
    }

    private FarmerPlanSummaryProjection createProjection(
            UUID planId, UUID cropId, String cropName, String cropVariety,
            String harvestType, String unit, String status, String locationDescription,
            BigDecimal plantedAreaHa, BigDecimal targetKg, BigDecimal realizedKg, BigDecimal pendingKg,
            LocalDate plantingDate, LocalDate harvestStart, LocalDate harvestEnd, LocalDate lastExecutionDate
    ) {
        return new FarmerPlanSummaryProjection() {
            @Override public UUID getPlanId() { return planId; }
            @Override public UUID getCropId() { return cropId; }
            @Override public String getCropName() { return cropName; }
            @Override public String getCropVariety() { return cropVariety; }
            @Override public String getHarvestType() { return harvestType; }
            @Override public String getUnit() { return unit; }
            @Override public String getStatus() { return status; }
            @Override public String getLocationDescription() { return locationDescription; }
            @Override public BigDecimal getPlantedAreaHa() { return plantedAreaHa; }
            @Override public BigDecimal getTargetKg() { return targetKg; }
            @Override public BigDecimal getRealizedKg() { return realizedKg; }
            @Override public BigDecimal getPendingKg() { return pendingKg; }
            @Override public LocalDate getPlantingDate() { return plantingDate; }
            @Override public LocalDate getExpectedHarvestStart() { return harvestStart; }
            @Override public LocalDate getExpectedHarvestEnd() { return harvestEnd; }
            @Override public LocalDate getLastExecutionDate() { return lastExecutionDate; }
        };
    }

    @Test
    @DisplayName("findPlanSummariesByFarmer: deve mapear campos da projeção para FarmerPlanSummary")
    void findPlanSummariesByFarmer_deveMapearCamposCorretamente() {
        FarmerPlanSummaryProjection projection = createProjection(
                planId,
                cropId,
                "Milho",
                "Híbrido",
                "SINGLE",
                "KG",
                "ACTIVE",
                "Talhão Sul",
                new BigDecimal("5.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("7500.00"),
                new BigDecimal("500.00"),
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 6, 20)
        );

        when(jpaRepository.findPlanSummariesByFarmer(farmerId, harvestId, false))
                .thenReturn(List.of(projection));

        List<FarmerPlanSummary> summaries = repository.findPlanSummariesByFarmer(farmerId, harvestId);

        assertThat(summaries).hasSize(1);
        FarmerPlanSummary summary = summaries.get(0);
        assertThat(summary.planId()).isEqualTo(planId);
        assertThat(summary.cropId()).isEqualTo(cropId);
        assertThat(summary.cropName()).isEqualTo("Milho");
        assertThat(summary.cropVariety()).isEqualTo("Híbrido");
        assertThat(summary.harvestType()).isEqualTo(HarvestType.SINGLE);
        assertThat(summary.unit()).isEqualTo("KG");
        assertThat(summary.status()).isEqualTo(PlanStatus.ACTIVE);
        assertThat(summary.locationDescription()).isEqualTo("Talhão Sul");
        assertThat(summary.plantedAreaHa()).isEqualByComparingTo("5.00");
        assertThat(summary.targetKg()).isEqualByComparingTo("10000.00");
        assertThat(summary.realizedKg()).isEqualByComparingTo("7500.00");
        assertThat(summary.pendingKg()).isEqualByComparingTo("500.00");
        assertThat(summary.plantingDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(summary.expectedHarvestStart()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(summary.expectedHarvestEnd()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(summary.lastExecutionDate()).isEqualTo(LocalDate.of(2026, 6, 20));
    }

    @Test
    @DisplayName("findPlanSummariesByFarmer: deve tratar valores nulos e inválidos com fallback seguro (SINGLE, KG, PLANNED)")
    void findPlanSummariesByFarmer_deveTratarNulosEInvalidosComFallback() {
        FarmerPlanSummaryProjection projection = createProjection(
                planId,
                cropId,
                "Mandioca",
                null,
                null, // harvestType nulo -> fallback para SINGLE
                null, // unit nula -> fallback para KG
                null, // status nulo -> fallback para PLANNED
                null,
                new BigDecimal("2.00"),
                null, // targetKg nulo
                null, // realizedKg nulo
                null, // pendingKg nulo
                null,
                null,
                null,
                null
        );

        when(jpaRepository.findPlanSummariesByFarmer(farmerId, null, true))
                .thenReturn(List.of(projection));

        List<FarmerPlanSummary> summaries = repository.findPlanSummariesByFarmer(farmerId, null, true);

        assertThat(summaries).hasSize(1);
        FarmerPlanSummary summary = summaries.get(0);
        assertThat(summary.harvestType()).isEqualTo(HarvestType.SINGLE);
        assertThat(summary.unit()).isEqualTo("KG");
        assertThat(summary.status()).isEqualTo(PlanStatus.PLANNED);
        assertThat(summary.targetKg()).isEqualTo(BigDecimal.ZERO);
        assertThat(summary.realizedKg()).isEqualTo(BigDecimal.ZERO);
        assertThat(summary.pendingKg()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("save: deve mapear entidade e persistir plano")
    void save_deveMapearEPersistir() {
        ProductionPlan plan = mock(ProductionPlan.class);
        ProductionPlanEntity entity = new ProductionPlanEntity();
        when(mapper.toEntity(plan)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(plan);

        ProductionPlan saved = repository.save(plan);

        assertThat(saved).isEqualTo(plan);
        verify(jpaRepository).save(entity);
    }

    @Test
    @DisplayName("findById: deve retornar plano quando existir")
    void findById_quandoExistir_deveRetornarPlano() {
        ProductionPlan plan = mock(ProductionPlan.class);
        ProductionPlanEntity entity = new ProductionPlanEntity();
        when(jpaRepository.findById(planId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(plan);

        Optional<ProductionPlan> result = repository.findById(planId);

        assertThat(result).contains(plan);
    }

    @Test
    @DisplayName("findAllByFarmerId: deve retornar lista mapeada")
    void findAllByFarmerId_deveRetornarListaMapeada() {
        ProductionPlan plan = mock(ProductionPlan.class);
        ProductionPlanEntity entity = new ProductionPlanEntity();
        when(jpaRepository.findAllByFarmerId(farmerId)).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(plan);

        List<ProductionPlan> result = repository.findAllByFarmerId(farmerId);

        assertThat(result).containsExactly(plan);
    }

    @Test
    @DisplayName("delete: deve invocar softDeleteById no repository JPA")
    void delete_deveInvocarSoftDelete() {
        repository.delete(planId);

        verify(jpaRepository).softDeleteById(planId);
    }
}
