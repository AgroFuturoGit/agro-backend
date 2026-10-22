package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.FarmerPlanSummary;
import com.ufal.smartagro.domain.model.ManagementCommunityCensusRow;
import com.ufal.smartagro.domain.model.ManagementOrgHarvestSummaryRow;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.TechnicianAssistedFarmerRow;
import com.ufal.smartagro.domain.model.TechnicianPlanSummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionPlanRepository {
    ProductionPlan save(ProductionPlan productionPlan);
    Optional<ProductionPlan> findById(UUID id);
    List<ProductionPlan> findAllByFarmerId(UUID farmerId);
    void delete(UUID id);
    boolean existsByHarvestId(UUID harvestId);
    boolean existsByCropId(UUID cropId);
    List<FarmerPlanSummary> findPlanSummariesByFarmer(UUID farmerId, UUID harvestId, boolean includeActiveContinuous);

    default List<FarmerPlanSummary> findPlanSummariesByFarmer(UUID farmerId, UUID harvestId) {
        return findPlanSummariesByFarmer(farmerId, harvestId, false);
    }

    List<TechnicianAssistedFarmerRow> findAssistedFarmersByTechnician(UUID technicianId, UUID communityId);

    List<TechnicianPlanSummary> findPlanSummariesByTechnician(
            UUID technicianId,
            UUID communityId,
            UUID cropId,
            UUID harvestId,
            boolean includeActiveContinuous
    );

    default List<TechnicianPlanSummary> findPlanSummariesByTechnician(
            UUID technicianId,
            UUID communityId,
            UUID cropId,
            UUID harvestId
    ) {
        return findPlanSummariesByTechnician(technicianId, communityId, cropId, harvestId, false);
    }

    List<ManagementCommunityCensusRow> findCommunityCensus(UUID organizationId, UUID communityId);

    List<ManagementOrgHarvestSummaryRow> findManagementHarvestSummary(
            UUID organizationId,
            UUID communityId,
            UUID harvestId
    );
}


