package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.mapper.ProductionPlanMapper;
import com.ufal.smartagro.adapters.out.persistence.projection.FarmerPlanSummaryProjection;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaProductionPlanRepository;
import com.ufal.smartagro.domain.model.FarmerPlanSummary;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductionPlanRepositoryImpl implements ProductionPlanRepository {

    private final JpaProductionPlanRepository jpaRepository;
    private final ProductionPlanMapper productionPlanMapper;

    @Override
    public ProductionPlan save(ProductionPlan productionPlan) {
        var entity = productionPlanMapper.toEntity(productionPlan);
        var savedEntity = jpaRepository.save(entity);
        return productionPlanMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ProductionPlan> findById(UUID id) {
        return jpaRepository.findById(id).map(productionPlanMapper::toDomain);
    }

    @Override
    public List<ProductionPlan> findAllByFarmerId(UUID farmerId) {
        return jpaRepository.findAllByFarmerId(farmerId).stream()
                .map(productionPlanMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.softDeleteById(id);
    }

    @Override
    public List<FarmerPlanSummary> findPlanSummariesByFarmer(UUID farmerId, UUID harvestId) {
        return jpaRepository.findPlanSummariesByFarmer(farmerId, harvestId).stream()
                .map(this::toFarmerPlanSummary)
                .toList();
    }

    private FarmerPlanSummary toFarmerPlanSummary(FarmerPlanSummaryProjection p) {
        HarvestType harvestType = null;
        if (p.getHarvestType() != null) {
            try {
                harvestType = HarvestType.valueOf(p.getHarvestType());
            } catch (IllegalArgumentException ignored) {
            }
        }

        PlanStatus status = null;
        if (p.getStatus() != null) {
            try {
                status = PlanStatus.valueOf(p.getStatus());
            } catch (IllegalArgumentException ignored) {
            }
        }

        BigDecimal targetKg = p.getTargetKg() != null ? p.getTargetKg() : BigDecimal.ZERO;
        BigDecimal realizedKg = p.getRealizedKg() != null ? p.getRealizedKg() : BigDecimal.ZERO;
        BigDecimal pendingKg = p.getPendingKg() != null ? p.getPendingKg() : BigDecimal.ZERO;

        return new FarmerPlanSummary(
                p.getPlanId(),
                p.getCropId(),
                p.getCropName(),
                p.getCropVariety(),
                harvestType,
                p.getUnit(),
                status,
                p.getLocationDescription(),
                p.getPlantedAreaHa(),
                targetKg,
                realizedKg,
                pendingKg,
                p.getPlantingDate(),
                p.getExpectedHarvestStart(),
                p.getExpectedHarvestEnd(),
                p.getLastExecutionDate()
        );
    }
}
