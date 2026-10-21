package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.FarmerPlanSummary;
import com.ufal.smartagro.domain.model.ProductionPlan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionPlanRepository {
    ProductionPlan save(ProductionPlan productionPlan);
    Optional<ProductionPlan> findById(UUID id);
    List<ProductionPlan> findAllByFarmerId(UUID farmerId);
    void delete(UUID id);
    List<FarmerPlanSummary> findPlanSummariesByFarmer(UUID farmerId, UUID harvestId, boolean includeActiveContinuous);

    default List<FarmerPlanSummary> findPlanSummariesByFarmer(UUID farmerId, UUID harvestId) {
        return findPlanSummariesByFarmer(farmerId, harvestId, false);
    }
}
