-- ============================================================================
-- AUDIT LOG (Tabela append-only e triggers de imutabilidade)
-- ============================================================================

CREATE TABLE audit_logs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL REFERENCES users(id),
    action       VARCHAR(50)  NOT NULL,      -- CREATE, UPDATE, DELETE, VALIDATE, REJECT, EXPORT
    target_table VARCHAR(100) NOT NULL,
    target_id    UUID,                        -- Opcional (nulo para ações globais/sem alvo único)
    details      JSONB,
    created_at   TIMESTAMP    NOT NULL DEFAULT now()
);

-- Índices otimizados para consultas operacionais e temporais
CREATE INDEX idx_audit_logs_target  ON audit_logs (target_table, target_id);
CREATE INDEX idx_audit_logs_user    ON audit_logs (user_id, created_at DESC);
CREATE INDEX idx_audit_logs_created ON audit_logs (created_at DESC);

-- Bloqueio de mutação diretamente no banco de dados (garantia append-only)
CREATE OR REPLACE FUNCTION audit_logs_block_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_logs é append-only: operação % não permitida', TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_logs_immutable
    BEFORE UPDATE OR DELETE ON audit_logs
    FOR EACH ROW EXECUTE FUNCTION audit_logs_block_mutation();

CREATE TRIGGER trg_audit_logs_no_truncate
    BEFORE TRUNCATE ON audit_logs
    FOR EACH STATEMENT EXECUTE FUNCTION audit_logs_block_mutation();

-- ============================================================================
-- PRODUCTION EXECUTIONS: Nova coluna e migração de dados legados
-- ============================================================================

ALTER TABLE production_executions ADD COLUMN rejection_reason TEXT;

-- Backfill: extrai o motivo que estava concatenado em notes e limpa as observações
UPDATE production_executions
SET rejection_reason = substring(notes FROM '\[Motivo da rejeição\]: (.*)$'),
    notes = NULLIF(btrim(regexp_replace(notes, '\s*\|?\s*\[Motivo da rejeição\]: .*$', '')), '')
WHERE status = 'REJECTED' AND notes LIKE '%[Motivo da rejeição]: %';
