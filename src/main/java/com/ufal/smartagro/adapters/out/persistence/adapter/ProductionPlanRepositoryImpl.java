package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.mapper.ProductionPlanMapper;
import com.ufal.smartagro.adapters.out.persistence.projection.FarmerPlanSummaryProjection;
import com.ufal.smartagro.adapters.out.persistence.projection.ManagementCommunityCensusProjection;
import com.ufal.smartagro.adapters.out.persistence.projection.ManagementOrgHarvestSummaryProjection;
import com.ufal.smartagro.adapters.out.persistence.projection.TechnicianAssistedFarmerProjection;
import com.ufal.smartagro.adapters.out.persistence.projection.TechnicianPlanSummaryProjection;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaProductionPlanRepository;
import com.ufal.smartagro.domain.model.FarmerPlanSummary;
import com.ufal.smartagro.domain.model.ManagementCommunityCensusRow;
import com.ufal.smartagro.domain.model.ManagementOrgHarvestSummaryRow;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.TechnicianAssistedFarmerRow;
import com.ufal.smartagro.domain.model.TechnicianPlanSummary;
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
    public boolean existsByHarvestId(UUID harvestId) {
        return jpaRepository.existsByHarvestId(harvestId);
    }

    @Override
    public boolean existsByCropId(UUID cropId) {
        return jpaRepository.existsByCropId(cropId);
    }

    @Override
    public long countByHarvestId(UUID harvestId) {
        return jpaRepository.countByHarvestId(harvestId);
    }

    @Override
    public long countByCropId(UUID cropId) {
        return jpaRepository.countByCropId(cropId);
    }

    @Override
    public List<FarmerPlanSummary> findPlanSummariesByFarmer(UUID farmerId, UUID harvestId, boolean includeActiveContinuous) {
        return jpaRepository.findPlanSummariesByFarmer(farmerId, harvestId, includeActiveContinuous).stream()
                .map(this::toFarmerPlanSummary)
                .toList();
    }

    @Override
    public List<FarmerPlanSummary> findPlanSummariesByFarmer(UUID farmerId, UUID harvestId) {
        return findPlanSummariesByFarmer(farmerId, harvestId, false);
    }

    private FarmerPlanSummary toFarmerPlanSummary(FarmerPlanSummaryProjection p) {
        HarvestType harvestType = HarvestType.SINGLE;
        if (p.getHarvestType() != null && !p.getHarvestType().isBlank()) {
            try {
                harvestType = HarvestType.valueOf(p.getHarvestType().trim());
            } catch (IllegalArgumentException ignored) {
                harvestType = HarvestType.SINGLE;
            }
        }

        String unit = (p.getUnit() != null && !p.getUnit().isBlank()) ? p.getUnit().trim() : "KG";

        PlanStatus status = PlanStatus.PLANNED;
        if (p.getStatus() != null && !p.getStatus().isBlank()) {
            try {
                status = PlanStatus.valueOf(p.getStatus().trim());
            } catch (IllegalArgumentException ignored) {
                status = PlanStatus.PLANNED;
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
                unit,
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

    @Override
    public List<TechnicianAssistedFarmerRow> findAssistedFarmersByTechnician(UUID technicianId, UUID communityId) {
        return jpaRepository.findAssistedFarmersByTechnician(technicianId, communityId).stream()
                .map(this::toTechnicianAssistedFarmerRow)
                .toList();
    }

    @Override
    public List<TechnicianPlanSummary> findPlanSummariesByTechnician(
            UUID technicianId,
            UUID communityId,
            UUID cropId,
            UUID harvestId,
            boolean includeActiveContinuous
    ) {
        return jpaRepository.findPlanSummariesByTechnician(technicianId, communityId, cropId, harvestId, includeActiveContinuous).stream()
                .map(this::toTechnicianPlanSummary)
                .toList();
    }

    @Override
    public List<TechnicianPlanSummary> findPlanSummariesByTechnician(
            UUID technicianId,
            UUID communityId,
            UUID cropId,
            UUID harvestId
    ) {
        return findPlanSummariesByTechnician(technicianId, communityId, cropId, harvestId, false);
    }

    private TechnicianAssistedFarmerRow toTechnicianAssistedFarmerRow(TechnicianAssistedFarmerProjection p) {
        return new TechnicianAssistedFarmerRow(
                p.getCommunityId(),
                p.getCommunityName(),
                p.getFarmerId(),
                p.getFarmerName(),
                p.getIsCompliant()
        );
    }

    private TechnicianPlanSummary toTechnicianPlanSummary(TechnicianPlanSummaryProjection p) {
        HarvestType harvestType = HarvestType.SINGLE;
        if (p.getHarvestType() != null && !p.getHarvestType().isBlank()) {
            try {
                harvestType = HarvestType.valueOf(p.getHarvestType().trim());
            } catch (IllegalArgumentException ignored) {
                harvestType = HarvestType.SINGLE;
            }
        }

        String unit = (p.getUnit() != null && !p.getUnit().isBlank()) ? p.getUnit().trim() : "KG";

        PlanStatus status = PlanStatus.PLANNED;
        if (p.getStatus() != null && !p.getStatus().isBlank()) {
            try {
                status = PlanStatus.valueOf(p.getStatus().trim());
            } catch (IllegalArgumentException ignored) {
                status = PlanStatus.PLANNED;
            }
        }

        BigDecimal plantedAreaHa = p.getPlantedAreaHa() != null ? p.getPlantedAreaHa() : BigDecimal.ZERO;
        BigDecimal targetKg = p.getTargetKg() != null ? p.getTargetKg() : BigDecimal.ZERO;
        BigDecimal realizedKg = p.getRealizedKg() != null ? p.getRealizedKg() : BigDecimal.ZERO;
        BigDecimal pendingKg = p.getPendingKg() != null ? p.getPendingKg() : BigDecimal.ZERO;
        long pendingExecutionsCount = p.getPendingExecutionsCount() != null ? p.getPendingExecutionsCount() : 0L;

        return new TechnicianPlanSummary(
                p.getPlanId(),
                p.getFarmerId(),
                p.getFarmerName(),
                p.getCommunityId(),
                p.getCommunityName(),
                p.getCropId(),
                p.getCropName(),
                p.getCropVariety(),
                harvestType,
                unit,
                status,
                plantedAreaHa,
                targetKg,
                realizedKg,
                pendingKg,
                pendingExecutionsCount,
                p.getPlantingDate(),
                p.getExpectedHarvestStart(),
                p.getExpectedHarvestEnd(),
                p.getLastExecutionDate()
        );
    }

    @Override
    public List<ManagementCommunityCensusRow> findCommunityCensus(UUID organizationId, UUID communityId) {
        return jpaRepository.findCommunityCensus(organizationId, communityId).stream()
                .map(this::toManagementCommunityCensusRow)
                .toList();
    }

    @Override
    public List<ManagementOrgHarvestSummaryRow> findManagementHarvestSummary(
            UUID organizationId,
            UUID communityId,
            UUID harvestId
    ) {
        return jpaRepository.findManagementHarvestSummary(organizationId, communityId, harvestId).stream()
                .map(this::toManagementOrgHarvestSummaryRow)
                .toList();
    }

    private ManagementCommunityCensusRow toManagementCommunityCensusRow(ManagementCommunityCensusProjection p) {
        long activeFarmers = p.getActiveFarmersCount() != null ? p.getActiveFarmersCount() : 0L;
        long compliantFarmers = p.getCompliantFarmersCount() != null ? p.getCompliantFarmersCount() : 0L;
        return new ManagementCommunityCensusRow(
                p.getOrganizationId(),
                p.getOrganizationName(),
                p.getCommunityId(),
                p.getCommunityName(),
                activeFarmers,
                compliantFarmers
        );
    }

    private ManagementOrgHarvestSummaryRow toManagementOrgHarvestSummaryRow(ManagementOrgHarvestSummaryProjection p) {
        long planCount = p.getPlanCount() != null ? p.getPlanCount() : 0L;
        long farmerCount = p.getFarmerCount() != null ? p.getFarmerCount() : 0L;
        BigDecimal plantedAreaHa = p.getPlantedAreaHa() != null ? p.getPlantedAreaHa() : BigDecimal.ZERO;
        BigDecimal targetKg = p.getTargetKg() != null ? p.getTargetKg() : BigDecimal.ZERO;
        BigDecimal realizedKg = p.getRealizedKg() != null ? p.getRealizedKg() : BigDecimal.ZERO;
        String unit = (p.getUnit() != null && !p.getUnit().isBlank()) ? p.getUnit().trim() : "KG";

        return new ManagementOrgHarvestSummaryRow(
                p.getOrganizationId(),
                p.getOrganizationName(),
                p.getCommunityId(),
                p.getCommunityName(),
                p.getCropId(),
                p.getCropName(),
                p.getCropVariety(),
                unit,
                p.getHarvestId(),
                planCount,
                farmerCount,
                plantedAreaHa,
                targetKg,
                realizedKg,
                p.getLastExecutionDate()
        );
    }
}
