package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.ProductionPlanEntity;
import com.ufal.smartagro.adapters.out.persistence.projection.FarmerPlanSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaProductionPlanRepository extends JpaRepository<ProductionPlanEntity, UUID> {
    List<ProductionPlanEntity> findAllByFarmerId(UUID farmerId);

    @Modifying
    @Query("UPDATE ProductionPlanEntity p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void softDeleteById(@Param("id") UUID id);

    @Query(value = """
            SELECT
                pp.id                                                                    AS "planId",
                c.id                                                                     AS "cropId",
                c.name                                                                   AS "cropName",
                c.variety                                                                AS "cropVariety",
                c.harvest_type                                                           AS "harvestType",
                c.unit                                                                   AS "unit",
                pp.status                                                                AS "status",
                pp.location_description                                                  AS "locationDescription",
                pp.planted_area                                                          AS "plantedAreaHa",
                COALESCE(pp.planted_area * pp.expected_productivity, 0)                  AS "targetKg",
                COALESCE(SUM(pe.quantity_kg) FILTER (WHERE pe.status = 'VALIDATED'), 0)    AS "realizedKg",
                COALESCE(SUM(pe.quantity_kg) FILTER (WHERE pe.status = 'PENDING'), 0)      AS "pendingKg",
                pp.planned_planting_date                                                 AS "plantingDate",
                pp.expected_harvest_start                                                AS "expectedHarvestStart",
                pp.expected_harvest_end                                                  AS "expectedHarvestEnd",
                MAX(pe.harvested_at)                                                     AS "lastExecutionDate"
            FROM production_plans pp
            JOIN crop c ON c.id = pp.crop_id AND c.deleted_at IS NULL
            LEFT JOIN production_executions pe
                   ON pe.production_plan_id = pp.id AND pe.deleted_at IS NULL
            WHERE pp.farmer_id = :farmerId
              AND pp.deleted_at IS NULL
              AND pp.status <> 'CANCELED'
              AND (
                    (CAST(:harvestId AS uuid) IS NULL)
                 OR (pp.harvest_id = CAST(:harvestId AS uuid))
                 OR (:includeActiveContinuous = true AND c.harvest_type = 'CONTINUOUS' AND pp.status = 'ACTIVE')
              )
            GROUP BY pp.id, pp.created_at, c.id, c.name, c.variety, c.harvest_type, c.unit
            ORDER BY pp.created_at DESC
            """, nativeQuery = true)
    List<FarmerPlanSummaryProjection> findPlanSummariesByFarmer(
            @Param("farmerId") UUID farmerId,
            @Param("harvestId") UUID harvestId,
            @Param("includeActiveContinuous") boolean includeActiveContinuous
    );
}
