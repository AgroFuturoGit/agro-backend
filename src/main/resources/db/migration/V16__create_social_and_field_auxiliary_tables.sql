-- Tabelas auxiliares socioeconômicas e de campo do produtor (Feature 5.5)

CREATE TABLE family_compositions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farmer_id UUID NOT NULL REFERENCES farmers(id),
    name VARCHAR(255) NOT NULL,
    kinship VARCHAR(100) NOT NULL,
    age INTEGER NOT NULL CHECK (age >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE financings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farmer_id UUID NOT NULL REFERENCES farmers(id),
    description VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE field_occurrences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id UUID NOT NULL REFERENCES production_executions(id),
    log_type VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    weather_observation VARCHAR(255),
    evidence_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_family_compositions_farmer_id ON family_compositions (farmer_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_financings_farmer_id ON financings (farmer_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_field_occurrences_execution_id ON field_occurrences (execution_id) WHERE deleted_at IS NULL;
