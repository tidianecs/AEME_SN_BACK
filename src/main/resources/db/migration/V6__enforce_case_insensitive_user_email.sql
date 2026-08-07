CREATE UNIQUE INDEX idx_profils_email_lower
ON profils_utilisateurs (LOWER(email));
