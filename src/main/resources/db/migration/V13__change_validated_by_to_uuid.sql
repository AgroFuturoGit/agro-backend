-- validated_by identifica o usuário responsável pela revisão; valores vazios
-- são tratados como ausência, e valores não UUID interrompem a migração.
DO $$
DECLARE
    invalid_values TEXT;
    orphan_values TEXT;
BEGIN
    SELECT string_agg(format('execution %s: %L', id, validated_by::text), ', ')
      INTO invalid_values
      FROM production_executions
     WHERE NULLIF(BTRIM(validated_by::text), '') IS NOT NULL
       AND NOT pg_input_is_valid(BTRIM(validated_by::text), 'uuid');

    IF invalid_values IS NOT NULL THEN
        RAISE EXCEPTION 'Cannot convert production_executions.validated_by to UUID; invalid values: %', invalid_values;
    END IF;

    SELECT string_agg(format('execution %s: %L', e.id, e.validated_by::text), ', ')
      INTO orphan_values
      FROM production_executions e
      LEFT JOIN users u
        ON u.id = NULLIF(BTRIM(e.validated_by::text), '')::UUID
     WHERE NULLIF(BTRIM(e.validated_by::text), '') IS NOT NULL
       AND u.id IS NULL;

    IF orphan_values IS NOT NULL THEN
        RAISE EXCEPTION 'Cannot add validated_by foreign key; user UUIDs do not exist: %', orphan_values;
    END IF;
END $$;

-- Remove eventual FK criada com nome ou tipo diferente antes de converter a coluna.
DO $$
DECLARE
    fk_name TEXT;
BEGIN
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
END $$;

ALTER TABLE production_executions
    ALTER COLUMN validated_by TYPE UUID
    USING NULLIF(BTRIM(validated_by::text), '')::UUID;

ALTER TABLE production_executions
    ADD CONSTRAINT fk_production_executions_validated_by
    FOREIGN KEY (validated_by) REFERENCES users(id);
