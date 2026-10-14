ALTER TABLE technical_assistances ADD COLUMN community_id UUID;

UPDATE technical_assistances ta
SET community_id = f.community_id
FROM farmers f
WHERE ta.farmer_id = f.id;

DELETE FROM technical_assistances WHERE community_id IS NULL;

ALTER TABLE technical_assistances ALTER COLUMN community_id SET NOT NULL;

ALTER TABLE technical_assistances DROP CONSTRAINT IF EXISTS fk_technical_assistance_farmer;
ALTER TABLE technical_assistances DROP COLUMN IF EXISTS farmer_id;

ALTER TABLE technical_assistances ADD CONSTRAINT fk_technical_assistance_community FOREIGN KEY (community_id) REFERENCES communities(id);
