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

    @org.springframework.data.jpa.repository.Query("SELECT p.keycloakId FROM ProfilUtilisateur p WHERE p.actif = true")
    java.util.List<UUID> findActiveGlobalChatMembers();

    @org.springframework.data.jpa.repository.Query("SELECT p.keycloakId FROM ProfilUtilisateur p WHERE p.actif = true AND p.cohorte.id = :cohorteId")
    java.util.List<UUID> findActiveMembersByCohorte(@org.springframework.data.repository.query.Param("cohorteId") Long cohorteId);

    @org.springframework.data.jpa.repository.Query("SELECT p.keycloakId FROM ProfilUtilisateur p WHERE p.actif = true AND p.structure.id = :structureId")
    java.util.List<UUID> findActiveMembersByStructure(@org.springframework.data.repository.query.Param("structureId") Long structureId);

    @org.springframework.data.jpa.repository.Query("SELECT p.keycloakId FROM ProfilUtilisateur p " +
            "LEFT JOIN p.structure s " +
            "LEFT JOIN s.ministereV2 sm " +
            "WHERE p.actif = true AND " +
            "((p.role = 'DAGE' AND p.ministere.id = :ministereId) OR " +
            "(p.role = 'GESTIONNAIRE' AND sm.id = :ministereId))")
    java.util.List<UUID> findActiveMembersByMinistere(@org.springframework.data.repository.query.Param("ministereId") Long ministereId);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM ProfilUtilisateur p WHERE p.actif = true AND p.keycloakId IN :keycloakIds")
    java.util.List<ProfilUtilisateur> findActiveProfilesByKeycloakIds(@org.springframework.data.repository.query.Param("keycloakIds") java.util.Collection<UUID> keycloakIds);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT p FROM ProfilUtilisateur p WHERE p.role = com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur.ADMIN ORDER BY p.id")
    java.util.List<ProfilUtilisateur> findAllAdminsForUpdate();

    @org.springframework.data.jpa.repository.Query("SELECT p.keycloakId FROM ProfilUtilisateur p WHERE p.actif = true AND p.role = com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur.ADMIN")
    java.util.List<UUID> findActiveAdmins();
}
