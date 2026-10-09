-- Evolução do núcleo agrícola. V10 já é usada pela refatoração de usuários;
-- esta migração aplica as mudanças de produção na sequência Flyway correta.

ALTER TABLE crop
    ADD COLUMN cycle_days INTEGER,
    ADD COLUMN expected_productivity NUMERIC(14, 4),
    ADD COLUMN harvest_type VARCHAR(30),
    ADD COLUMN unit VARCHAR(30),
    ADD COLUMN unit_weight_kg NUMERIC(12, 4);

ALTER TABLE production_plans
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',
    ADD COLUMN expected_harvest_start DATE,
    ADD COLUMN expected_harvest_end DATE,
    ADD COLUMN expected_productivity NUMERIC(14, 4),
    ADD COLUMN location_description VARCHAR(255),
    ADD COLUMN latitude NUMERIC(9, 6),
    ADD COLUMN longitude NUMERIC(9, 6);

-- Preserva o rendimento previamente planejado como produtividade por área.
UPDATE production_plans
SET expected_productivity = expected_yield / NULLIF(planted_area, 0);

ALTER TABLE production_plans ALTER COLUMN expected_productivity SET NOT NULL;
ALTER TABLE production_plans DROP COLUMN expected_yield;

ALTER TABLE production_executions
    ADD COLUMN quantity NUMERIC(14, 4),
    ADD COLUMN quantity_kg NUMERIC(14, 4),
    ADD COLUMN harvested_at TIMESTAMP,
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN validated_by VARCHAR(255),
    ADD COLUMN validated_at TIMESTAMP,
    ADD COLUMN notes TEXT;

UPDATE production_executions
SET quantity = actual_yield,
    quantity_kg = actual_yield,
    harvested_at = harvest_date::timestamp;

ALTER TABLE production_executions
    ALTER COLUMN quantity SET NOT NULL,
    ALTER COLUMN quantity_kg SET NOT NULL,
    ALTER COLUMN harvested_at SET NOT NULL,
    DROP COLUMN actual_yield,
    DROP COLUMN harvest_date;

ALTER TABLE production_plans
    ADD CONSTRAINT chk_production_plan_latitude CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    ADD CONSTRAINT chk_production_plan_longitude CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
    ADD CONSTRAINT chk_production_plan_location_pair CHECK ((latitude IS NULL) = (longitude IS NULL));
