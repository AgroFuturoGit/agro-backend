package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.ProductionExecutionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaProductionExecutionRepository extends JpaRepository<ProductionExecutionEntity, UUID> {
    @Override
    @EntityGraph(attributePaths = {"productionPlan", "productionPlan.farmer", "productionPlan.crop", "productionPlan.harvest", "validatedBy"})
    Optional<ProductionExecutionEntity> findById(UUID id);

    @EntityGraph(attributePaths = {"productionPlan", "productionPlan.farmer", "productionPlan.crop", "productionPlan.harvest", "validatedBy"})
    List<ProductionExecutionEntity> findAllByProductionPlanId(UUID planId);

    @EntityGraph(attributePaths = {"productionPlan", "productionPlan.farmer", "productionPlan.crop", "productionPlan.harvest", "validatedBy"})
    Optional<ProductionExecutionEntity> findByProductionPlanIdAndClientId(UUID planId, UUID clientId);

    @Modifying
    @Query("UPDATE ProductionExecutionEntity p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
