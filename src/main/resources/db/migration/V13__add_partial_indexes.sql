-- ============================================================================
-- ÍNDICES PARCIAIS (Soft Delete: deleted_at IS NULL)
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_farmers_community_active
    ON farmers (community_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_communities_org_active
    ON communities (organization_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_managers_org_active
    ON managers (organization_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_plans_farmer_active
    ON production_plans (farmer_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_plans_harvest_active
    ON production_plans (harvest_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_plans_crop_active
    ON production_plans (crop_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_executions_plan_harvested_active
    ON production_executions (production_plan_id, harvested_at)
    WHERE deleted_at IS NULL;

-- Índice com cobertura (INCLUDE) para acelerar agregações na view analítica
CREATE INDEX IF NOT EXISTS idx_executions_plan_validated_active
    ON production_executions (production_plan_id) INCLUDE (quantity_kg)
    WHERE deleted_at IS NULL AND status = 'VALIDATED';

CREATE INDEX IF NOT EXISTS idx_ta_technician_community_active
    ON technical_assistances (technician_id, community_id)
    WHERE deleted_at IS NULL;
