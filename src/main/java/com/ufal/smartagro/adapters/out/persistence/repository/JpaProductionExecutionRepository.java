package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.ProductionExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaProductionExecutionRepository extends JpaRepository<ProductionExecutionEntity, UUID> {
    java.util.List<ProductionExecutionEntity> findAllByProductionPlanId(UUID planId);

    @Modifying
    @Query("UPDATE ProductionExecutionEntity p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
