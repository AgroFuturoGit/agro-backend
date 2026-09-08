package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.ProductionPlan;

import java.util.Optional;
import java.util.UUID;

public interface ProductionPlanRepository {
    ProductionPlan save(ProductionPlan productionPlan);
    Optional<ProductionPlan> findById(UUID id);
    java.util.List<ProductionPlan> findAllByFarmerId(UUID farmerId);
    void delete(UUID id);
}
