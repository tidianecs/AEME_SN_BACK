package com.ditix.backend.ProfilUtilisateur.Services;

import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Repository.CohorteRepository;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import com.ditix.backend.ProfilUtilisateur.DTO.CreerUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.DTO.CreationUtilisateurResponse;
import com.ditix.backend.ProfilUtilisateur.DTO.ProfilUtilisateurDTO;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@Service
public class CreationUtilisateurOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(CreationUtilisateurOrchestrator.class);

    private final GestionCompteKeycloakService gestionCompteKeycloakService;
    private final SauvegardeProfilService sauvegardeProfilService;
    private final ProfilUtilisateurRepository profilUtilisateurRepository;
    private final MinistereRepository ministereRepository;
    private final StructureRepository structureRepository;
    private final CohorteRepository cohorteRepository;
    private final com.ditix.backend.Chat.Services.ChatUserMembershipService chatUserMembershipService;

    public CreationUtilisateurOrchestrator(
            GestionCompteKeycloakService gestionCompteKeycloakService,
            SauvegardeProfilService sauvegardeProfilService,
            ProfilUtilisateurRepository profilUtilisateurRepository,
            MinistereRepository ministereRepository,
            StructureRepository structureRepository,
            CohorteRepository cohorteRepository,
            com.ditix.backend.Chat.Services.ChatUserMembershipService chatUserMembershipService) {
        this.gestionCompteKeycloakService = gestionCompteKeycloakService;
        this.sauvegardeProfilService = sauvegardeProfilService;
        this.profilUtilisateurRepository = profilUtilisateurRepository;
        this.ministereRepository = ministereRepository;
        this.structureRepository = structureRepository;
        this.cohorteRepository = cohorteRepository;
        this.chatUserMembershipService = chatUserMembershipService;
    }

    @Transactional
    public CreationUtilisateurResponse creerUtilisateur(CreerUtilisateurRequest request) {
        // Normalisation
        request.setPrenom(request.getPrenom() != null ? request.getPrenom().trim() : null);
        request.setNom(request.getNom() != null ? request.getNom().trim() : null);
        request.setEmail(request.getEmail() != null ? request.getEmail().trim().toLowerCase(Locale.ROOT) : null);
        if (request.getEmailSecondaire() != null && !request.getEmailSecondaire().isBlank()) {
            request.setEmailSecondaire(request.getEmailSecondaire().trim().toLowerCase(Locale.ROOT));
        } else {
            request.setEmailSecondaire(null);
        }

        // Vérification Email Postgres
        if (profilUtilisateurRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'email est déjà utilisé (PostgreSQL).");
        }

        // Vérification Email Keycloak
        if (gestionCompteKeycloakService.emailExiste(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'email est déjà utilisé (Keycloak).");
        }

        Ministere ministere = null;
        Structure structure = null;
        Cohorte cohorte = null;
        String roleKeycloak = "user";

        // Validation selon rôle
        if (request.getRole() == RoleUtilisateur.ADMIN) {
            if (request.getMinistereId() != null || request.getStructureId() != null || request.getCohorteId() != null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Un ADMIN ne doit avoir ni ministère, ni structure, ni cohorte.");
            }
            roleKeycloak = "admin";
        } else if (request.getRole() == RoleUtilisateur.DAGE) {
            if (request.getMinistereId() == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Un DAGE doit avoir un ministère.");
            }
            if (request.getStructureId() != null || request.getCohorteId() != null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Un DAGE ne doit avoir ni structure ni cohorte.");
            }
            ministere = ministereRepository.findById(request.getMinistereId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ministère introuvable."));
            if (ministere.getActif() == null || !ministere.getActif()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le ministère est inactif.");
            }
            if (profilUtilisateurRepository.existsByRoleAndMinistereIdAndActifTrue(RoleUtilisateur.DAGE, ministere.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Un DAGE actif existe déjà pour ce ministère.");
            }
        } else if (request.getRole() == RoleUtilisateur.GESTIONNAIRE) {
            if (request.getStructureId() == null || request.getCohorteId() == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Un GESTIONNAIRE doit avoir une structure et une cohorte.");
            }
            if (request.getMinistereId() != null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le ministereId ne doit pas être fourni pour un GESTIONNAIRE.");
            }
            structure = structureRepository.findById(request.getStructureId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Structure introuvable."));
            if (structure.getActif() == null || !structure.getActif()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "La structure est inactive.");
            }
            if (structure.getMinistereV2() == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "La structure n'a pas de ministère V2 associé.");
            }
            cohorte = cohorteRepository.findById(request.getCohorteId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cohorte introuvable."));
            if (cohorte.getActif() == null || !cohorte.getActif()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "La cohorte est inactive.");
            }
        }

        UUID keycloakId = null;
        ProfilUtilisateur profil = null;
        try {
            // Création Keycloak
            keycloakId = gestionCompteKeycloakService.creerIdentite(
                    request.getEmail(), request.getPrenom(), request.getNom(), roleKeycloak);

            // Sauvegarde Postgres
            profil = sauvegardeProfilService.sauvegarder(request, keycloakId, ministere, structure, cohorte);

            try {
                chatUserMembershipService.syncUserMemberships(profil);
            } catch (Exception chatError) {
                logger.error("Erreur lors de la synchronisation des memberships de chat profilId={}, keycloakId={}, role={}", profil.getId(), keycloakId, profil.getRole(), chatError);
            }

            boolean invitationEnvoyee;
            try {
                gestionCompteKeycloakService.envoyerActionsInitiales(keycloakId);
                invitationEnvoyee = true;
            } catch (Exception emailError) {
                logger.error("Erreur lors de l'envoi de l'invitation Keycloak pour {}", request.getEmail(), emailError);
                invitationEnvoyee = false;
            }

            return new CreationUtilisateurResponse(new ProfilUtilisateurDTO(profil), invitationEnvoyee);
        } catch (Exception erreurPrincipale) {
            if (keycloakId != null && profil == null) {
                try {
                    gestionCompteKeycloakService.supprimerIdentite(keycloakId);
                } catch (Exception compensationError) {
                    logger.error("CRITICAL: COMPTE_ORPHELIN_KEYCLOAK keycloakId={}", keycloakId, compensationError);
                }
            }
            throw erreurPrincipale;
        }
    }
}
