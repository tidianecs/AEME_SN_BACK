-- 1. Table profils_utilisateurs
CREATE TABLE profils_utilisateurs (
    id BIGSERIAL PRIMARY KEY,

    keycloak_id UUID NOT NULL UNIQUE,

    prenom VARCHAR(150) NOT NULL,
    nom VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,

    telephone_principal VARCHAR(30) NULL,
    telephone_secondaire VARCHAR(30) NULL,
    email_secondaire VARCHAR(255) NULL,

    genre VARCHAR(20) NULL,
    date_naissance DATE NULL,

    departement_administratif VARCHAR(150) NULL,
    poste_occupe VARCHAR(150) NULL,

    date_nomination DATE NULL,
    date_installation DATE NULL,
    date_formation DATE NULL,
    derniere_mise_a_niveau DATE NULL,

    role VARCHAR(30) NOT NULL,

    ministere_id BIGINT NULL,
    structure_id BIGINT NULL,
    cohorte_id BIGINT NULL,

    actif BOOLEAN NOT NULL DEFAULT TRUE,

    cree_le TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Foreign keys
ALTER TABLE profils_utilisateurs
    ADD CONSTRAINT fk_profils_ministere FOREIGN KEY (ministere_id) REFERENCES ministeres(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_profils_structure FOREIGN KEY (structure_id) REFERENCES structures(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_profils_cohorte FOREIGN KEY (cohorte_id) REFERENCES cohortes(id) ON DELETE RESTRICT;

-- 3. Constraint Role
ALTER TABLE profils_utilisateurs ADD CONSTRAINT chk_profils_roles CHECK (
    (role = 'ADMIN' AND ministere_id IS NULL AND structure_id IS NULL AND cohorte_id IS NULL) OR
    (role = 'DAGE' AND ministere_id IS NOT NULL AND structure_id IS NULL AND cohorte_id IS NULL) OR
    (role = 'GESTIONNAIRE' AND ministere_id IS NULL AND structure_id IS NOT NULL AND cohorte_id IS NOT NULL)
);

-- 4. Index DAGE UNIQUE
CREATE UNIQUE INDEX idx_unique_dage_per_ministere
    ON profils_utilisateurs(ministere_id)
    WHERE role = 'DAGE' AND actif = TRUE;

-- 5. Explicit Indexes
CREATE INDEX idx_profils_structure_id ON profils_utilisateurs(structure_id);
CREATE INDEX idx_profils_cohorte_id ON profils_utilisateurs(cohorte_id);
