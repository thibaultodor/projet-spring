-- Ajoute les signatures aux tables existantes sans modifier leurs données.
USE recensement;

ALTER TABLE ville
    ADD COLUMN IF NOT EXISTS user_maj VARCHAR(50) NULL,
    ADD COLUMN IF NOT EXISTS date_maj DATETIME(6) NULL;

ALTER TABLE departement
    ADD COLUMN IF NOT EXISTS user_maj VARCHAR(50) NULL,
    ADD COLUMN IF NOT EXISTS date_maj DATETIME(6) NULL;
