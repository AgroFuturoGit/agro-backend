-- ====================================================================
-- V10: Refatoração e desacoplamento de Users, Farmers e Technicians
-- ProduPlan v2.0 / Issue #15
-- ====================================================================

-- 1. Tabela users: remover dados pessoais (ficam em farmers) e tornar cpf opcional
ALTER TABLE users DROP COLUMN IF EXISTS date_of_birth;
ALTER TABLE users ALTER COLUMN cpf DROP NOT NULL;

-- 2. Tabela farmers: renomear alias_name, tornar user_id e community_id opcionais, adicionar dados cadastrais
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'farmers' AND column_name = 'alias_name'
    ) THEN
        ALTER TABLE farmers RENAME COLUMN alias_name TO local_name;
    END IF;
END $$;

ALTER TABLE farmers ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE farmers ALTER COLUMN community_id DROP NOT NULL;

ALTER TABLE farmers ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS cpf VARCHAR(14);
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS mother_name VARCHAR(255);
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS origin VARCHAR(255);
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS education_level VARCHAR(255);
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS street VARCHAR(255);
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS city VARCHAR(255);
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS state VARCHAR(255);
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS ibge_code VARCHAR(20);
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS latitude NUMERIC(9, 6);
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS longitude NUMERIC(9, 6);
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS registration_source VARCHAR(50);
ALTER TABLE farmers ADD COLUMN IF NOT EXISTS created_by UUID;

-- Migração de dados de users para farmers existentes
UPDATE farmers f
SET full_name = u.full_name,
    cpf = u.cpf,
    registration_source = 'MANAGER'
FROM users u
WHERE f.user_id = u.id;

UPDATE farmers
SET full_name = 'Agricultor'
WHERE full_name IS NULL;

WITH numbered AS (
    SELECT id, LPAD(CAST(ROW_NUMBER() OVER () AS VARCHAR), 11, '0') AS generated_cpf
    FROM farmers
    WHERE cpf IS NULL
)
UPDATE farmers f
SET cpf = n.generated_cpf
FROM numbered n
WHERE f.id = n.id;

UPDATE farmers
SET registration_source = 'MANAGER'
WHERE registration_source IS NULL;

ALTER TABLE farmers ALTER COLUMN full_name SET NOT NULL;
ALTER TABLE farmers ALTER COLUMN cpf SET NOT NULL;
ALTER TABLE farmers ALTER COLUMN registration_source SET NOT NULL;

ALTER TABLE farmers DROP CONSTRAINT IF EXISTS uq_farmers_cpf;
ALTER TABLE farmers ADD CONSTRAINT uq_farmers_cpf UNIQUE (cpf);

ALTER TABLE farmers DROP CONSTRAINT IF EXISTS fk_farmer_created_by;
ALTER TABLE farmers ADD CONSTRAINT fk_farmer_created_by FOREIGN KEY (created_by) REFERENCES users(id);

-- 3. Tabela technicians: substituir professional_id por registration_type e registration_number; adicionar created_by
ALTER TABLE technicians ADD COLUMN IF NOT EXISTS registration_type VARCHAR(50);
ALTER TABLE technicians ADD COLUMN IF NOT EXISTS registration_number VARCHAR(100);
ALTER TABLE technicians ADD COLUMN IF NOT EXISTS created_by UUID;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'technicians' AND column_name = 'professional_id'
    ) THEN
        UPDATE technicians
        SET registration_type = CASE
                WHEN professional_id ILIKE 'CREA-%' THEN 'CREA'
                WHEN professional_id ILIKE 'CFT-%' THEN 'CFT'
                WHEN professional_id ILIKE 'CFTA-%' THEN 'CFTA'
                ELSE 'CREA'
            END,
            registration_number = CASE
                WHEN professional_id ILIKE 'CREA-%' THEN SUBSTRING(professional_id FROM 6)
                WHEN professional_id ILIKE 'CFT-%' THEN SUBSTRING(professional_id FROM 5)
                WHEN professional_id ILIKE 'CFTA-%' THEN SUBSTRING(professional_id FROM 6)
                ELSE COALESCE(professional_id, 'UNKNOWN')
            END,
            created_by = user_id
        WHERE registration_type IS NULL OR registration_number IS NULL OR created_by IS NULL;
    ELSE
        UPDATE technicians
        SET registration_type = 'CREA',
            registration_number = 'UNKNOWN',
            created_by = user_id
        WHERE registration_type IS NULL OR registration_number IS NULL OR created_by IS NULL;
    END IF;
END $$;

ALTER TABLE technicians ALTER COLUMN registration_type SET NOT NULL;
ALTER TABLE technicians ALTER COLUMN registration_number SET NOT NULL;
ALTER TABLE technicians ALTER COLUMN created_by SET NOT NULL;

ALTER TABLE technicians DROP CONSTRAINT IF EXISTS fk_technician_created_by;
ALTER TABLE technicians ADD CONSTRAINT fk_technician_created_by FOREIGN KEY (created_by) REFERENCES users(id);

ALTER TABLE technicians DROP COLUMN IF EXISTS professional_id;
