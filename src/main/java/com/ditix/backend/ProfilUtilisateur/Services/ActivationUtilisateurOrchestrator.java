package com.ditix.backend.ProfilUtilisateur.Services;

import com.ditix.backend.Auth.Services.AuthService;
import com.ditix.backend.ProfilUtilisateur.DTO.ActivationUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ActivationUtilisateurOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(ActivationUtilisateurOrchestrator.class);

    private final ProfilUtilisateurRepository profilUtilisateurRepository;
    private final AuthService authService;
    private final ActivationUtilisateurLocalService activationUtilisateurLocalService;

    public ActivationUtilisateurOrchestrator(
            ProfilUtilisateurRepository profilUtilisateurRepository,
            AuthService authService,
            ActivationUtilisateurLocalService activationUtilisateurLocalService) {
        this.profilUtilisateurRepository = profilUtilisateurRepository;
        this.authService = authService;
        this.activationUtilisateurLocalService = activationUtilisateurLocalService;
    }

    public void activerUtilisateur(Long targetId, ActivationUtilisateurRequest request, ProfilUtilisateur actor) {
        ProfilUtilisateur target = profilUtilisateurRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (target.getId().equals(actor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Impossible de modifier son propre statut d'activation");
        }

        if (target.getKeycloakId() == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Utilisateur sans identité Keycloak");
        }

        if (Boolean.TRUE.equals(target.getActif()) == request.getActif()) {
            return; // Idempotent
        }

        String keycloakId = target.getKeycloakId().toString();
        boolean previousKeycloakState = authService.isUserEnabled(keycloakId);

        if (previousKeycloakState == request.getActif()) {
            // Out of sync? Just ensure DB matches requested state
            logger.warn("Keycloak state is already {}, syncing PostgreSQL to match", request.getActif());
        }

        authService.setUserEnabled(keycloakId, request.getActif());

        try {
            activationUtilisateurLocalService.processLocalActivation(targetId, request);
        } catch (Exception ex) {
            try {
                authService.setUserEnabled(keycloakId, previousKeycloakState);
                logger.info("Successfully compensated Keycloak state back to {} for user {}", previousKeycloakState, keycloakId);
            } catch (Exception compEx) {
                logger.error("Failed to compensate Keycloak state for profileId={}, keycloakId={}, requestedState={}, compensationState={}",
                        targetId, keycloakId, request.getActif(), previousKeycloakState, compEx);
            }
            throw ex;
        }
    }
}
