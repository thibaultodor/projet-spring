-- Crée les comptes de démonstration sans modifier ceux qui existent déjà.
USE recensement;

CREATE TABLE IF NOT EXISTS role (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS utilisateur (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cette table relie chaque utilisateur à ses rôles.
CREATE TABLE IF NOT EXISTS utilisateur_role (
    utilisateur_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (utilisateur_id, role_id),
    CONSTRAINT fk_utilisateur_role_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id),
    CONSTRAINT fk_utilisateur_role_role FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

START TRANSACTION;

INSERT INTO role (nom)
SELECT 'ROLE_USER' WHERE NOT EXISTS (SELECT 1 FROM role WHERE nom = 'ROLE_USER');
INSERT INTO role (nom)
SELECT 'ROLE_ADMIN' WHERE NOT EXISTS (SELECT 1 FROM role WHERE nom = 'ROLE_ADMIN');

-- Les empreintes ont été générées avec BCryptPasswordEncoder.
-- user : user1234 ; admin : admin1234.
INSERT INTO utilisateur (username, password)
SELECT 'user', '$2a$10$eZm0WeOLNAVWVguipc1nSedUzrNG9NkIXwb1gobxrrWrbAqsGpf2S'
WHERE NOT EXISTS (SELECT 1 FROM utilisateur WHERE username = 'user');
INSERT INTO utilisateur (username, password)
SELECT 'admin', '$2a$10$mOIkDCMc6fwhtaFDaAzrDedLtXqotXTvdKT/rvKzEJz46e7xTWnWy'
WHERE NOT EXISTS (SELECT 1 FROM utilisateur WHERE username = 'admin');

INSERT INTO utilisateur_role (utilisateur_id, role_id)
SELECT u.id, r.id FROM utilisateur u JOIN role r ON r.nom = 'ROLE_USER'
WHERE u.username = 'user' AND NOT EXISTS (
    SELECT 1 FROM utilisateur_role ur WHERE ur.utilisateur_id = u.id AND ur.role_id = r.id
);
INSERT INTO utilisateur_role (utilisateur_id, role_id)
SELECT u.id, r.id FROM utilisateur u JOIN role r ON r.nom = 'ROLE_ADMIN'
WHERE u.username = 'admin' AND NOT EXISTS (
    SELECT 1 FROM utilisateur_role ur WHERE ur.utilisateur_id = u.id AND ur.role_id = r.id
);

COMMIT;
