-- V7: Add profil_utilisateur_id to reports

ALTER TABLE reports
    ADD COLUMN profil_utilisateur_id BIGINT NULL;

ALTER TABLE reports
    ADD CONSTRAINT fk_reports_profil_utilisateur
    FOREIGN KEY (profil_utilisateur_id)
    REFERENCES profils_utilisateurs(id)
    ON DELETE RESTRICT;

-- Backfill ONLY for existing reports where created_by_user_id matches a Keycloak ID in profils_utilisateurs
UPDATE reports r
SET profil_utilisateur_id = p.id
FROM profils_utilisateurs p
WHERE r.profil_utilisateur_id IS NULL
  AND p.keycloak_id::text = r.created_by_user_id;

CREATE INDEX idx_reports_profil_utilisateur_id ON reports(profil_utilisateur_id);
