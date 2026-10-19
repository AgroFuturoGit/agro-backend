-- ============================================================================
-- ATENÇÃO (PostgreSQL DDL Lock):
-- Colunas utilizadas nestas views não podem sofrer DROP COLUMN ou ALTER TYPE
-- em migrações futuras sem antes derrubar as views com CASCADE e recriá-las.
-- ============================================================================

-- 1. Visão de Agricultores Ativos (expondo is_compliant)
CREATE OR REPLACE VIEW vw_active_farmers AS
SELECT f.id,
       f.user_id,
       f.community_id,
       f.full_name,
       f.cpf,
       f.phone,
       f.local_name,
       f.street,
       f.city,
       f.state,
       f.ibge_code,
       f.latitude,
       f.longitude,
       f.registration_source,
       f.created_by,
       f.is_compliant,
       f.created_at,
       f.updated_at
FROM farmers f
WHERE f.deleted_at IS NULL;

-- 2. Visão de Progresso por Plano de Produção
CREATE OR REPLACE VIEW vw_plan_progress AS
SELECT pp.id                                                        AS plan_id,
       pp.farmer_id,
       af.community_id,
       c.organization_id,
       pp.harvest_id,
       pp.crop_id,
       pp.status                                                    AS plan_status,
       pp.planted_area,
       pp.expected_productivity,
       (pp.planted_area * pp.expected_productivity)                 AS target_kg,
       COALESCE(e.realized_kg, 0)                                   AS realized_kg,
       CASE WHEN pp.planted_area * pp.expected_productivity > 0
            THEN ROUND(COALESCE(e.realized_kg, 0) * 100
                       / (pp.planted_area * pp.expected_productivity), 2)
       END                                                          AS completion_pct,
       e.last_execution_date
FROM production_plans pp
JOIN vw_active_farmers af ON af.id = pp.farmer_id
LEFT JOIN communities c   ON c.id = af.community_id AND c.deleted_at IS NULL
LEFT JOIN LATERAL (
    SELECT SUM(x.quantity_kg) FILTER (WHERE x.status = 'VALIDATED') AS realized_kg,
           MAX(x.harvested_at)                                      AS last_execution_date
    FROM production_executions x
    WHERE x.production_plan_id = pp.id AND x.deleted_at IS NULL
) e ON TRUE
WHERE pp.deleted_at IS NULL;

-- 3. Visão Materializada Consolidada da Safra (Grão: org x comunidade x cultura x safra)
CREATE MATERIALIZED VIEW mv_org_harvest_summary AS
SELECT p.organization_id,
       p.community_id,
       p.crop_id,
       p.harvest_id,
       COUNT(*)                                                     AS plan_count,
       COUNT(DISTINCT p.farmer_id)                                  AS farmer_count,
       SUM(p.planted_area)                                          AS planted_area_ha,
       SUM(p.target_kg)                                             AS target_kg,
       SUM(p.realized_kg)                                           AS realized_kg,
       CASE WHEN SUM(p.target_kg) > 0
            THEN ROUND(SUM(p.realized_kg) * 100 / SUM(p.target_kg), 2)
       END                                                          AS completion_pct,
       MAX(p.last_execution_date)                                   AS last_execution_date
FROM vw_plan_progress p
WHERE p.organization_id IS NOT NULL
  AND p.community_id    IS NOT NULL
  AND p.plan_status <> 'CANCELED'
GROUP BY p.organization_id, p.community_id, p.crop_id, p.harvest_id
WITH DATA;

-- Índice único mandatório para REFRESH MATERIALIZED VIEW CONCURRENTLY
CREATE UNIQUE INDEX uq_mv_org_harvest_summary
    ON mv_org_harvest_summary (organization_id, community_id, crop_id, harvest_id);

CREATE INDEX idx_mv_org_harvest_summary_org_harvest
    ON mv_org_harvest_summary (organization_id, harvest_id);
