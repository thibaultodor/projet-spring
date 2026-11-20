-- Installation neuve avec MariaDB 10.4 (XAMPP).
-- La collation utf8mb4_0900_ai_ci de MySQL 8 n'existe pas sur cette version de MariaDB.
-- Sur MySQL 8, cette collation peut remplacer utf8mb4_unicode_ci ci-dessous.
-- Ce script n'est pas exécuté au démarrage de l'application.
CREATE DATABASE IF NOT EXISTS recensement CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE recensement;

CREATE TABLE IF NOT EXISTS region (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS departement (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    ID_REGION INT NOT NULL,
    NOM VARCHAR(100),
    CONSTRAINT fk_departement_region FOREIGN KEY (ID_REGION) REFERENCES region(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ville (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    population INT NOT NULL,
    ID_DEPARTEMENT INT NOT NULL,
    CONSTRAINT fk_ville_departement FOREIGN KEY (ID_DEPARTEMENT) REFERENCES departement(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Fournit un premier rattachement uniquement s'il n'est pas déjà présent.
INSERT INTO region (code, nom)
SELECT '76', 'Occitanie' WHERE NOT EXISTS (SELECT 1 FROM region WHERE code = '76');
INSERT INTO departement (code, NOM, ID_REGION)
SELECT '34', 'Hérault', r.id FROM region r
WHERE r.code = '76' AND NOT EXISTS (SELECT 1 FROM departement WHERE code = '34');
