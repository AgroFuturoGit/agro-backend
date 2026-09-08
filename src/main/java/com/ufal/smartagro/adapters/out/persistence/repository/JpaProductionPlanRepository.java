package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.ProductionPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaProductionPlanRepository extends JpaRepository<ProductionPlanEntity, UUID> {
    java.util.List<ProductionPlanEntity> findAllByFarmerId(UUID farmerId);

    @Modifying
    @Query("UPDATE ProductionPlanEntity p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void softDeleteById(@Param("id") UUID id);
}
