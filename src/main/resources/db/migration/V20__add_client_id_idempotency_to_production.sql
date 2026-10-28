-- Idempotência offline-first para planos de produção, apontamentos e ocorrências (MOB-04, MOB-15, MOB-20, DOC-07)
--
-- O client_id é gerado no dispositivo móvel antes do envio. Permite que retentativas de rede
-- retornem a entidade já persistida sem criar duplicatas no banco de dados.

-- 1. Planos de Produção
ALTER TABLE production_plans
    ADD COLUMN client_id UUID;

CREATE UNIQUE INDEX uq_production_plans_client_id
    ON production_plans (farmer_id, client_id)
    WHERE client_id IS NOT NULL AND deleted_at IS NULL;

-- 2. Apontamentos de Produção (Execuções)
ALTER TABLE production_executions
    ADD COLUMN client_id UUID;

CREATE UNIQUE INDEX uq_production_executions_client_id
    ON production_executions (production_plan_id, client_id)
    WHERE client_id IS NOT NULL AND deleted_at IS NULL;

-- 3. Ocorrências de Campo
ALTER TABLE field_occurrences
    ADD COLUMN client_id UUID;

CREATE UNIQUE INDEX uq_field_occurrences_client_id
    ON field_occurrences (execution_id, client_id)
    WHERE client_id IS NOT NULL AND deleted_at IS NULL;
