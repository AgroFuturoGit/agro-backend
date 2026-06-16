package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.ProductionExecution;

import java.util.Optional;
import java.util.UUID;

public interface ProductionExecutionRepository {
    ProductionExecution save(ProductionExecution productionExecution);
    Optional<ProductionExecution> findById(UUID id);
    java.util.List<ProductionExecution> findAllByProductionPlanId(UUID planId);
    void delete(UUID id);
}
