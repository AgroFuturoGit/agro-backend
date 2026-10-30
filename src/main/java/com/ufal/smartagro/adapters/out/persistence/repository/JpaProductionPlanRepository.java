package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.ProductionPlanEntity;
import com.ufal.smartagro.adapters.out.persistence.projection.FarmerPlanSummaryProjection;
import com.ufal.smartagro.adapters.out.persistence.projection.ManagementCommunityCensusProjection;
import com.ufal.smartagro.adapters.out.persistence.projection.ManagementOrgHarvestSummaryProjection;
import com.ufal.smartagro.adapters.out.persistence.projection.TechnicianAssistedFarmerProjection;
import com.ufal.smartagro.adapters.out.persistence.projection.TechnicianPlanSummaryProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaProductionPlanRepository extends JpaRepository<ProductionPlanEntity, UUID> {
    @Override
    @EntityGraph(attributePaths = {"farmer", "crop", "harvest"})
    Optional<ProductionPlanEntity> findById(UUID id);

    @EntityGraph(attributePaths = {"farmer", "crop", "harvest"})
    List<ProductionPlanEntity> findAllByFarmerId(UUID farmerId);

    @EntityGraph(attributePaths = {"farmer", "crop", "harvest"})
    Optional<ProductionPlanEntity> findByFarmerIdAndClientId(UUID farmerId, UUID clientId);

    boolean existsByHarvestId(UUID harvestId);

    boolean existsByCropId(UUID cropId);

    long countByHarvestId(UUID harvestId);

    long countByCropId(UUID cropId);

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

    @Query(value = """
            SELECT
                comm.id          AS "communityId",
                comm.name        AS "communityName",
                af.id            AS "farmerId",
                af.full_name     AS "farmerName",
                af.is_compliant  AS "isCompliant"
            FROM technical_assistances ta
            JOIN communities comm
                   ON comm.id = ta.community_id AND comm.deleted_at IS NULL
            LEFT JOIN vw_active_farmers af
                   ON af.community_id = comm.id
            WHERE ta.technician_id = :technicianId
              AND ta.end_date IS NULL
              AND ta.deleted_at IS NULL
              AND (CAST(:communityId AS uuid) IS NULL OR comm.id = CAST(:communityId AS uuid))
            ORDER BY comm.name ASC, af.full_name ASC
            """, nativeQuery = true)
    List<TechnicianAssistedFarmerProjection> findAssistedFarmersByTechnician(
            @Param("technicianId") UUID technicianId,
            @Param("communityId") UUID communityId
    );

    @Query(value = """
            SELECT
                pp.id                                                                    AS "planId",
                af.id                                                                    AS "farmerId",
                af.full_name                                                             AS "farmerName",
                comm.id                                                                  AS "communityId",
                comm.name                                                                AS "communityName",
                c.id                                                                     AS "cropId",
                c.name                                                                   AS "cropName",
                c.variety                                                                AS "cropVariety",
                c.harvest_type                                                           AS "harvestType",
                c.unit                                                                   AS "unit",
                pp.status                                                                AS "status",
                pp.planted_area                                                          AS "plantedAreaHa",
                COALESCE(pp.planted_area * pp.expected_productivity, 0)                  AS "targetKg",
                COALESCE(SUM(pe.quantity_kg) FILTER (WHERE pe.status = 'VALIDATED'), 0)  AS "realizedKg",
                COALESCE(SUM(pe.quantity_kg) FILTER (WHERE pe.status = 'PENDING'), 0)    AS "pendingKg",
                COUNT(pe.id) FILTER (WHERE pe.status = 'PENDING')                        AS "pendingExecutionsCount",
                pp.planned_planting_date                                                 AS "plantingDate",
                pp.expected_harvest_start                                                AS "expectedHarvestStart",
                pp.expected_harvest_end                                                  AS "expectedHarvestEnd",
                MAX(pe.harvested_at)                                                     AS "lastExecutionDate"
            FROM technical_assistances ta
            JOIN communities comm
                   ON comm.id = ta.community_id AND comm.deleted_at IS NULL
            JOIN vw_active_farmers af
                   ON af.community_id = comm.id
            JOIN production_plans pp
                   ON pp.farmer_id = af.id AND pp.deleted_at IS NULL AND pp.status <> 'CANCELED'
            JOIN crop c
                   ON c.id = pp.crop_id AND c.deleted_at IS NULL
            LEFT JOIN production_executions pe
                   ON pe.production_plan_id = pp.id AND pe.deleted_at IS NULL
            WHERE ta.technician_id = :technicianId
              AND ta.end_date IS NULL
              AND ta.deleted_at IS NULL
              AND (CAST(:communityId AS uuid) IS NULL OR comm.id = CAST(:communityId AS uuid))
              AND (CAST(:cropId AS uuid) IS NULL OR c.id = CAST(:cropId AS uuid))
              AND (
                    (CAST(:harvestId AS uuid) IS NULL)
                 OR (pp.harvest_id = CAST(:harvestId AS uuid))
                 OR (:includeActiveContinuous = true AND c.harvest_type = 'CONTINUOUS' AND pp.status = 'ACTIVE')
              )
            GROUP BY pp.id, pp.created_at, af.id, af.full_name, comm.id, comm.name, c.id, c.name, c.variety, c.harvest_type, c.unit
            ORDER BY comm.name ASC, af.full_name ASC, pp.created_at DESC
            """, nativeQuery = true)
    List<TechnicianPlanSummaryProjection> findPlanSummariesByTechnician(
            @Param("technicianId") UUID technicianId,
            @Param("communityId") UUID communityId,
            @Param("cropId") UUID cropId,
            @Param("harvestId") UUID harvestId,
            @Param("includeActiveContinuous") boolean includeActiveContinuous
    );

    @Query(value = """
            SELECT
                org.id                                                           AS "organizationId",
                org.name                                                         AS "organizationName",
                comm.id                                                          AS "communityId",
                comm.name                                                        AS "communityName",
                COUNT(DISTINCT af.id)                                            AS "activeFarmersCount",
                COUNT(DISTINCT af.id) FILTER (WHERE af.is_compliant = true)      AS "compliantFarmersCount"
            FROM organizations org
            JOIN communities comm
                   ON comm.organization_id = org.id AND comm.deleted_at IS NULL
            LEFT JOIN vw_active_farmers af
                   ON af.community_id = comm.id
            WHERE org.deleted_at IS NULL
              AND (CAST(:organizationId AS uuid) IS NULL OR org.id = CAST(:organizationId AS uuid))
              AND (CAST(:communityId AS uuid) IS NULL OR comm.id = CAST(:communityId AS uuid))
            GROUP BY org.id, org.name, comm.id, comm.name
            ORDER BY org.name ASC, comm.name ASC
            """, nativeQuery = true)
    List<ManagementCommunityCensusProjection> findCommunityCensus(
            @Param("organizationId") UUID organizationId,
            @Param("communityId") UUID communityId
    );

    @Query(value = """
            SELECT
                mv.organization_id     AS "organizationId",
                org.name               AS "organizationName",
                mv.community_id        AS "communityId",
                comm.name              AS "communityName",
                mv.crop_id             AS "cropId",
                c.name                 AS "cropName",
                c.variety              AS "cropVariety",
                c.unit                 AS "unit",
                mv.harvest_id          AS "harvestId",
                mv.plan_count          AS "planCount",
                mv.farmer_count        AS "farmerCount",
                mv.planted_area_ha     AS "plantedAreaHa",
                mv.target_kg           AS "targetKg",
                mv.realized_kg         AS "realizedKg",
                mv.last_execution_date AS "lastExecutionDate"
            FROM mv_org_harvest_summary mv
            JOIN organizations org ON org.id = mv.organization_id AND org.deleted_at IS NULL
            JOIN communities comm  ON comm.id = mv.community_id AND comm.deleted_at IS NULL
            JOIN crop c            ON c.id = mv.crop_id AND c.deleted_at IS NULL
            WHERE (CAST(:organizationId AS uuid) IS NULL OR mv.organization_id = CAST(:organizationId AS uuid))
              AND (CAST(:communityId AS uuid) IS NULL OR mv.community_id = CAST(:communityId AS uuid))
              AND (CAST(:harvestId AS uuid) IS NULL OR mv.harvest_id = CAST(:harvestId AS uuid))
            """, nativeQuery = true)
    List<ManagementOrgHarvestSummaryProjection> findManagementHarvestSummary(
            @Param("organizationId") UUID organizationId,
            @Param("communityId") UUID communityId,
            @Param("harvestId") UUID harvestId
    );
}
