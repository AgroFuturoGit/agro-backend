-- A entidade usa LocalDate, portanto harvested_at deve ser DATE no PostgreSQL.
-- Converte bancos que tenham recebido a coluna como TIMESTAMP em uma versão anterior.
ALTER TABLE production_executions
    ALTER COLUMN harvested_at TYPE DATE
    USING harvested_at::DATE;
