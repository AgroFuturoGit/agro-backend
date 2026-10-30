-- ============================================================================
-- V17: Índices únicos parciais (Soft Delete) e normalização de e-mail/CPF
-- Resolve: API-09, API-13, MOB-26, BE-Q-08
-- ============================================================================

-- 1. Remover constraints UNIQUE globais que impediam recriação após Soft Delete (API-13, BE-Q-08)
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_cpf_key;
ALTER TABLE farmers DROP CONSTRAINT IF EXISTS uq_farmers_cpf;

-- 2. Normalizar e-mails (minúsculas e sem espaços) e CPFs (apenas dígitos) (API-09, MOB-26)
UPDATE users
SET email = LOWER(BTRIM(email))
WHERE email <> LOWER(BTRIM(email));

UPDATE users
SET cpf = NULLIF(REGEXP_REPLACE(cpf, '[^0-9]', '', 'g'), '')
WHERE cpf IS NOT NULL;

UPDATE farmers
SET cpf = REGEXP_REPLACE(cpf, '[^0-9]', '', 'g')
WHERE cpf IS NOT NULL AND cpf <> REGEXP_REPLACE(cpf, '[^0-9]', '', 'g');

-- 3. Caso existam duplicatas ativas residuais pós-normalização, manter o mais antigo ativo
WITH dup_emails AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY LOWER(email) ORDER BY created_at ASC, id ASC) AS rn
    FROM users
    WHERE deleted_at IS NULL
)
UPDATE users u
SET deleted_at = CURRENT_TIMESTAMP
FROM dup_emails d
WHERE u.id = d.id AND d.rn > 1;

WITH dup_user_cpfs AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY cpf ORDER BY created_at ASC, id ASC) AS rn
    FROM users
    WHERE deleted_at IS NULL AND cpf IS NOT NULL
)
UPDATE users u
SET deleted_at = CURRENT_TIMESTAMP
FROM dup_user_cpfs d
WHERE u.id = d.id AND d.rn > 1;

WITH dup_farmer_cpfs AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY cpf ORDER BY created_at ASC, id ASC) AS rn
    FROM farmers
    WHERE deleted_at IS NULL
)
UPDATE farmers f
SET deleted_at = CURRENT_TIMESTAMP
FROM dup_farmer_cpfs d
WHERE f.id = d.id AND d.rn > 1;

-- 4. Criar índices únicos parciais respeitando o Soft Delete (WHERE deleted_at IS NULL)
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email_active
    ON users (LOWER(email))
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_cpf_active
    ON users (cpf)
    WHERE deleted_at IS NULL AND cpf IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_farmers_cpf_active
    ON farmers (cpf)
    WHERE deleted_at IS NULL;
