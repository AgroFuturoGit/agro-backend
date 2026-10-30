package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.ProductionExecution;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionExecutionRepository {
    ProductionExecution save(ProductionExecution productionExecution);
    Optional<ProductionExecution> findById(UUID id);
    List<ProductionExecution> findAllByProductionPlanId(UUID planId);
    Optional<ProductionExecution> findByPlanIdAndClientId(UUID planId, UUID clientId);
    void delete(UUID id);
}
