-- ============================================================================
-- V19: Índice único parcial em Crop, CHECK constraints de domínio e índices parciais de FK
-- Resolve: BE-Q-08, BE-Q-06
-- ============================================================================

-- 1. Remover eventuais constraints UNIQUE legadas em crop que impediam recriação após soft delete
ALTER TABLE crop DROP CONSTRAINT IF EXISTS uk_crop_name_variety;
ALTER TABLE crop DROP CONSTRAINT IF EXISTS uq_crop_name_variety;

-- Normalização de espaços em branco em crop
UPDATE crop
SET name = BTRIM(name),
    variety = BTRIM(variety)
WHERE name <> BTRIM(name) OR (variety IS NOT NULL AND variety <> BTRIM(variety));

-- Tratar duplicatas ativas residuais antes da criação do índice único parcial
WITH dup_crops AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY LOWER(BTRIM(name)), LOWER(COALESCE(BTRIM(variety), ''))
               ORDER BY created_at ASC, id ASC
           ) AS rn
    FROM crop
    WHERE deleted_at IS NULL
)
UPDATE crop c
SET deleted_at = CURRENT_TIMESTAMP
FROM dup_crops d
WHERE c.id = d.id AND d.rn > 1;

-- Criar índice único parcial case-insensitive respeitando Soft Delete (WHERE deleted_at IS NULL)
CREATE UNIQUE INDEX IF NOT EXISTS uq_crop_name_variety_active
    ON crop (LOWER(BTRIM(name)), LOWER(COALESCE(BTRIM(variety), '')))
    WHERE deleted_at IS NULL;

-- 2. Constraints CHECK de integridade de domínio
-- 2.1. Harvests: start_date <= end_date
UPDATE harvests
SET end_date = start_date
WHERE start_date > end_date;

ALTER TABLE harvests DROP CONSTRAINT IF EXISTS chk_harvests_date_order;
ALTER TABLE harvests ADD CONSTRAINT chk_harvests_date_order CHECK (start_date <= end_date);

-- 2.2. Production plans: planted_area > 0
UPDATE production_plans
SET planted_area = 0.01
WHERE planted_area <= 0;

ALTER TABLE production_plans DROP CONSTRAINT IF EXISTS chk_production_plans_positive_area;
ALTER TABLE production_plans ADD CONSTRAINT chk_production_plans_positive_area CHECK (planted_area > 0);

-- 2.3. Production executions: quantity > 0 AND quantity_kg > 0
UPDATE production_executions
SET quantity = 0.01
WHERE quantity <= 0;

UPDATE production_executions
SET quantity_kg = 0.01
WHERE quantity_kg <= 0;

ALTER TABLE production_executions DROP CONSTRAINT IF EXISTS chk_production_executions_positive_qty;
ALTER TABLE production_executions ADD CONSTRAINT chk_production_executions_positive_qty CHECK (quantity > 0 AND quantity_kg > 0);

-- 3. Índices parciais de integridade e desempenho em chaves estrangeiras (Soft Delete)
CREATE INDEX IF NOT EXISTS idx_farmers_user_id_active
    ON farmers (user_id)
    WHERE user_id IS NOT NULL AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_technicians_user_id_active
    ON technicians (user_id)
    WHERE user_id IS NOT NULL AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_managers_user_id_active
    ON managers (user_id)
    WHERE user_id IS NOT NULL AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_executions_validated_by_active
    ON production_executions (validated_by)
    WHERE validated_by IS NOT NULL AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_farmers_created_by_active
    ON farmers (created_by)
    WHERE created_by IS NOT NULL AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_technicians_created_by_active
    ON technicians (created_by)
    WHERE created_by IS NOT NULL AND deleted_at IS NULL;
