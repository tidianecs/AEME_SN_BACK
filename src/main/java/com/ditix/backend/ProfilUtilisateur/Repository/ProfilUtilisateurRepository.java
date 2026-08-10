package com.ditix.backend.ProfilUtilisateur.Repository;

import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfilUtilisateurRepository extends JpaRepository<ProfilUtilisateur, Long> {

    Optional<ProfilUtilisateur> findByKeycloakId(UUID keycloakId);

    Optional<ProfilUtilisateur> findByEmailIgnoreCase(String email);
    Optional<ProfilUtilisateur> findByEmail(String email);

    boolean existsByRoleAndMinistereIdAndActifTrue(com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur role, Long ministereId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"ministere", "structure", "structure.ministereV2", "cohorte"})
    org.springframework.data.domain.Page<ProfilUtilisateur> findAll(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"ministere", "structure", "structure.ministereV2", "cohorte"})
    Optional<ProfilUtilisateur> findById(Long id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"ministere", "structure", "structure.ministereV2", "cohorte"})
    @org.springframework.data.jpa.repository.Query("SELECT p FROM ProfilUtilisateur p WHERE p.role = 'GESTIONNAIRE' AND p.structure.ministereV2.id = :ministereId")
    org.springframework.data.domain.Page<ProfilUtilisateur> findGestionnairesByMinistereId(@org.springframework.data.repository.query.Param("ministereId") Long ministereId, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"ministere", "structure", "structure.ministereV2", "cohorte"})
    @org.springframework.data.jpa.repository.Query("SELECT p FROM ProfilUtilisateur p WHERE p.role = 'GESTIONNAIRE' AND p.structure.id = :structureId")
    org.springframework.data.domain.Page<ProfilUtilisateur> findGestionnairesByStructureId(@org.springframework.data.repository.query.Param("structureId") Long structureId, org.springframework.data.domain.Pageable pageable);
}
