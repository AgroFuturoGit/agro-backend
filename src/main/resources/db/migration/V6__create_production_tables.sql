CREATE TABLE production_plans (
    id UUID PRIMARY KEY,
    producer_id UUID NOT NULL,
    harvest_id UUID NOT NULL,
    crop_id UUID NOT NULL,
    planted_area NUMERIC(10, 2) NOT NULL,
    expected_yield NUMERIC(10, 2) NOT NULL,
    planned_calendar JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_production_plan_producer FOREIGN KEY (producer_id) REFERENCES producers(id),
    CONSTRAINT fk_production_plan_harvest FOREIGN KEY (harvest_id) REFERENCES harvests(id),
    CONSTRAINT fk_production_plan_crop FOREIGN KEY (crop_id) REFERENCES crop(id)
);

CREATE TABLE production_executions (
    id UUID PRIMARY KEY,
    production_plan_id UUID NOT NULL,
    actual_yield NUMERIC(10, 2) NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_execution_production_plan FOREIGN KEY (production_plan_id) REFERENCES production_plans(id)
);
