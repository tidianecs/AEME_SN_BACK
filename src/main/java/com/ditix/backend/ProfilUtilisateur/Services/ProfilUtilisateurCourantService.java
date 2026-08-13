package com.ditix.backend.ProfilUtilisateur.Services;

import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import com.ditix.backend.Auth.Services.AuthService;
import com.ditix.backend.ProfilUtilisateur.DTO.ModifierMonProfilRequest;
import com.ditix.backend.ProfilUtilisateur.DTO.ProfilUtilisateurDTO;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProfilUtilisateurCourantService {

    private final ProfilUtilisateurRepository profilUtilisateurRepository;
    private final AuthService authService;

    public ProfilUtilisateurCourantService(ProfilUtilisateurRepository profilUtilisateurRepository, AuthService authService) {
        this.profilUtilisateurRepository = profilUtilisateurRepository;
        this.authService = authService;
    }

    public ProfilUtilisateur obtenirProfilCourant(JwtAuthenticationToken authentication) {
        if (authentication == null || authentication.getToken() == null || authentication.getToken().getSubject() == null || authentication.getToken().getSubject().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
        }

        String sub = authentication.getToken().getSubject();
        UUID keycloakId;
        try {
            keycloakId = UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
        }

        ProfilUtilisateur profil = profilUtilisateurRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé"));

        if (!Boolean.TRUE.equals(profil.getActif())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
        }

        return profil;
    }

    @Transactional
    public ProfilUtilisateurDTO modifierProfilCourant(JwtAuthenticationToken authentication, ModifierMonProfilRequest request) {
        ProfilUtilisateur profil = obtenirProfilCourant(authentication);

        Map<String, String> keycloakUpdates = new HashMap<>();
        if (request.getPrenom() != null) {
            if (request.getPrenom().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le prénom ne peut pas être vide");
            }
            profil.setPrenom(request.getPrenom());
            keycloakUpdates.put("firstName", request.getPrenom());
        }
        if (request.getNom() != null) {
            if (request.getNom().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom ne peut pas être vide");
            }
            profil.setNom(request.getNom());
            keycloakUpdates.put("lastName", request.getNom());
        }

        if (request.getTelephonePrincipal() != null) profil.setTelephonePrincipal(request.getTelephonePrincipal());
        if (request.getTelephoneSecondaire() != null) profil.setTelephoneSecondaire(request.getTelephoneSecondaire());
        if (request.getEmailSecondaire() != null) profil.setEmailSecondaire(request.getEmailSecondaire());
        if (request.getGenre() != null) profil.setGenre(request.getGenre());
        if (request.getDateNaissance() != null) profil.setDateNaissance(request.getDateNaissance());
        if (request.getDepartementAdministratif() != null) profil.setDepartementAdministratif(request.getDepartementAdministratif());
        if (request.getPosteOccupe() != null) profil.setPosteOccupe(request.getPosteOccupe());
        if (request.getDateNomination() != null) profil.setDateNomination(request.getDateNomination());
        if (request.getDateInstallation() != null) profil.setDateInstallation(request.getDateInstallation());
        if (request.getDateFormation() != null) profil.setDateFormation(request.getDateFormation());
        if (request.getDerniereMiseANiveau() != null) profil.setDerniereMiseANiveau(request.getDerniereMiseANiveau());

        profil = profilUtilisateurRepository.save(profil);

        if (!keycloakUpdates.isEmpty()) {
            try {
                authService.updateUserProfile(profil.getKeycloakId().toString(), keycloakUpdates);
            } catch (Exception e) {
                // If Keycloak fails, the transaction will rollback, keeping PostgreSQL and Keycloak consistent
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la synchronisation avec Keycloak");
            }
        }

        return new ProfilUtilisateurDTO(profil);
    }
}
