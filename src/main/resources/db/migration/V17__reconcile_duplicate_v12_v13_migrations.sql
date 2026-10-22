-- Consolidates the changes from the duplicated V12 and V13 scripts.
-- The original scripts are preserved under db/migration-archive and are not
-- scanned as executable Flyway migrations.

-- V12: audit log and immutable history.
CREATE TABLE IF NOT EXISTS audit_logs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id),
    action       VARCHAR(50) NOT NULL,
    target_table VARCHAR(100) NOT NULL,
    target_id    UUID,
    details      JSONB,
    created_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_target
    ON audit_logs (target_table, target_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user
    ON audit_logs (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created
    ON audit_logs (created_at DESC);

CREATE OR REPLACE FUNCTION audit_logs_block_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_logs é append-only: operação % não permitida', TG_OP;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_audit_logs_immutable ON audit_logs;
CREATE TRIGGER trg_audit_logs_immutable
    BEFORE UPDATE OR DELETE ON audit_logs
    FOR EACH ROW EXECUTE FUNCTION audit_logs_block_mutation();

DROP TRIGGER IF EXISTS trg_audit_logs_no_truncate ON audit_logs;
CREATE TRIGGER trg_audit_logs_no_truncate
    BEFORE TRUNCATE ON audit_logs
    FOR EACH STATEMENT EXECUTE FUNCTION audit_logs_block_mutation();

ALTER TABLE production_executions
    ADD COLUMN IF NOT EXISTS rejection_reason TEXT;

UPDATE production_executions
   SET rejection_reason = substring(notes FROM '\[Motivo da rejeição\]: (.*)$'),
       notes = NULLIF(btrim(regexp_replace(notes, '\s*\|?\s*\[Motivo da rejeição\]: .*$', '')), '')
 WHERE status = 'REJECTED'
   AND notes LIKE '%[Motivo da rejeição]: %'
   AND rejection_reason IS NULL;

-- V12: align harvested_at with the LocalDate model, when an older database
-- still has the column as a timestamp.
-- V14 views depend on this column, so drop and recreate dependent views around the type change.
DROP MATERIALIZED VIEW IF EXISTS mv_org_harvest_summary;
DROP VIEW IF EXISTS vw_plan_progress;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM information_schema.columns
         WHERE table_schema = current_schema()
           AND table_name = 'production_executions'
           AND column_name = 'harvested_at'
           AND data_type LIKE 'timestamp%'
    ) THEN
        ALTER TABLE production_executions
            ALTER COLUMN harvested_at TYPE DATE
            USING harvested_at::DATE;
    END IF;
END $$;

-- V13: convert legacy text values and ensure the user foreign key exists.
DO $$
DECLARE
    column_type TEXT;
    fk_name TEXT;
BEGIN
    SELECT format_type(a.atttypid, a.atttypmod)
      INTO column_type
      FROM pg_attribute a
     WHERE a.attrelid = 'production_executions'::regclass
       AND a.attname = 'validated_by'
       AND NOT a.attisdropped;

    IF column_type IS NOT NULL AND column_type <> 'uuid' THEN
        FOR fk_name IN
            SELECT c.conname
              FROM pg_constraint c
              JOIN pg_attribute a
                ON a.attrelid = c.conrelid
               AND a.attname = 'validated_by'
             WHERE c.conrelid = 'production_executions'::regclass
               AND c.contype = 'f'
               AND a.attnum = ANY (c.conkey)
        LOOP
            EXECUTE format('ALTER TABLE production_executions DROP CONSTRAINT %I', fk_name);
        END LOOP;

        ALTER TABLE production_executions
            ALTER COLUMN validated_by TYPE UUID
            USING NULLIF(BTRIM(validated_by::text), '')::UUID;
    END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint c
          JOIN pg_attribute a
            ON a.attrelid = c.conrelid
           AND a.attname = 'validated_by'
         WHERE c.conrelid = 'production_executions'::regclass
           AND c.contype = 'f'
           AND a.attnum = ANY (c.conkey)
    ) THEN
        ALTER TABLE production_executions
            ADD CONSTRAINT fk_production_executions_validated_by
            FOREIGN KEY (validated_by) REFERENCES users(id);
    END IF;
END $$;

-- V13: indexes for active (non-deleted) records.
CREATE INDEX IF NOT EXISTS idx_farmers_community_active
    ON farmers (community_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_communities_org_active
    ON communities (organization_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_managers_org_active
    ON managers (organization_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_plans_farmer_active
    ON production_plans (farmer_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_plans_harvest_active
    ON production_plans (harvest_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_plans_crop_active
    ON production_plans (crop_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_executions_plan_harvested_active
    ON production_executions (production_plan_id, harvested_at)
    WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_executions_plan_validated_active
    ON production_executions (production_plan_id) INCLUDE (quantity_kg)
    WHERE deleted_at IS NULL AND status = 'VALIDATED';
CREATE INDEX IF NOT EXISTS idx_ta_technician_community_active
    ON technical_assistances (technician_id, community_id)
    WHERE deleted_at IS NULL;

-- Restore the V14 views after the possible harvested_at type change.
CREATE OR REPLACE VIEW vw_plan_progress AS
SELECT pp.id AS plan_id,
       pp.farmer_id,
       af.community_id,
       c.organization_id,
       pp.harvest_id,
       pp.crop_id,
       pp.status AS plan_status,
       pp.planted_area,
       pp.expected_productivity,
       (pp.planted_area * pp.expected_productivity) AS target_kg,
       COALESCE(e.realized_kg, 0) AS realized_kg,
       CASE WHEN pp.planted_area * pp.expected_productivity > 0
            THEN ROUND(COALESCE(e.realized_kg, 0) * 100
                       / (pp.planted_area * pp.expected_productivity), 2)
       END AS completion_pct,
       e.last_execution_date
FROM production_plans pp
JOIN vw_active_farmers af ON af.id = pp.farmer_id
LEFT JOIN communities c ON c.id = af.community_id AND c.deleted_at IS NULL
LEFT JOIN LATERAL (
    SELECT SUM(x.quantity_kg) FILTER (WHERE x.status = 'VALIDATED') AS realized_kg,
           MAX(x.harvested_at) AS last_execution_date
    FROM production_executions x
    WHERE x.production_plan_id = pp.id AND x.deleted_at IS NULL
) e ON TRUE
WHERE pp.deleted_at IS NULL;

CREATE MATERIALIZED VIEW mv_org_harvest_summary AS
SELECT p.organization_id,
       p.community_id,
       p.crop_id,
       p.harvest_id,
       COUNT(*) AS plan_count,
       COUNT(DISTINCT p.farmer_id) AS farmer_count,
       SUM(p.planted_area) AS planted_area_ha,
       SUM(p.target_kg) AS target_kg,
       SUM(p.realized_kg) AS realized_kg,
       CASE WHEN SUM(p.target_kg) > 0
            THEN ROUND(SUM(p.realized_kg) * 100 / SUM(p.target_kg), 2)
       END AS completion_pct,
       MAX(p.last_execution_date) AS last_execution_date
FROM vw_plan_progress p
WHERE p.organization_id IS NOT NULL
  AND p.community_id IS NOT NULL
  AND p.plan_status <> 'CANCELED'
GROUP BY p.organization_id, p.community_id, p.crop_id, p.harvest_id
WITH DATA;

CREATE UNIQUE INDEX uq_mv_org_harvest_summary
    ON mv_org_harvest_summary (organization_id, community_id, crop_id, harvest_id);
CREATE INDEX idx_mv_org_harvest_summary_org_harvest
    ON mv_org_harvest_summary (organization_id, harvest_id);
