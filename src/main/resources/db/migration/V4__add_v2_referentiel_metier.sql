-- 1. Table ministeres
CREATE TABLE ministeres (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    nom VARCHAR(255) NOT NULL UNIQUE,
    nom_court VARCHAR(100) NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    cree_le TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Table cohortes
CREATE TABLE cohortes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL UNIQUE,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    cree_le TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed cohortes
INSERT INTO cohortes (code, nom) VALUES ('COHORTE_1', 'Cohorte 1');
INSERT INTO cohortes (code, nom) VALUES ('COHORTE_2', 'Cohorte 2');

-- 3. Ajout des colonnes V2 à structures
ALTER TABLE structures
    ADD COLUMN code VARCHAR(80) NULL,
    ADD COLUMN ministere_id BIGINT NULL,
    ADD COLUMN latitude_v2 DOUBLE PRECISION NULL,
    ADD COLUMN longitude_v2 DOUBLE PRECISION NULL,
    ADD COLUMN departement VARCHAR(100) NULL,
    ADD COLUMN commune VARCHAR(100) NULL,
    ADD COLUMN adresse VARCHAR(255) NULL,
    ADD COLUMN categorie VARCHAR(100) NULL,
    ADD COLUMN actif BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN cree_le TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN modifie_le TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Foreign key pour ministere_id
ALTER TABLE structures
    ADD CONSTRAINT fk_structures_ministere
    FOREIGN KEY (ministere_id) REFERENCES ministeres(id) ON DELETE RESTRICT;

CREATE INDEX idx_structures_ministere_id
    ON structures(ministere_id);

-- Contraintes sur latitude_v2 et longitude_v2
ALTER TABLE structures
    ADD CONSTRAINT chk_latitude_v2 CHECK (latitude_v2 IS NULL OR (latitude_v2 >= -90 AND latitude_v2 <= 90)),
    ADD CONSTRAINT chk_longitude_v2 CHECK (longitude_v2 IS NULL OR (longitude_v2 >= -180 AND longitude_v2 <= 180));
