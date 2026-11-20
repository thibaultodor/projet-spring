-- Rend la région facultative pour créer un département à partir de son seul code.
-- Les régions déjà associées aux départements restent inchangées.
-- La clé étrangère est conservée : une région renseignée doit toujours exister.
USE recensement;
ALTER TABLE departement MODIFY COLUMN ID_REGION INT NULL;
