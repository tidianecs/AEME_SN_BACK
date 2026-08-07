package com.ditix.backend.ProfilUtilisateur.Repository;

import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfilUtilisateurRepository extends JpaRepository<ProfilUtilisateur, Long> {

    Optional<ProfilUtilisateur> findByKeycloakId(UUID keycloakId);

    Optional<ProfilUtilisateur> findByEmail(String email);
}
