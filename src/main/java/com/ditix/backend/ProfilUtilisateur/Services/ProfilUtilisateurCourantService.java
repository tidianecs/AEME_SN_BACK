package com.ditix.backend.ProfilUtilisateur.Services;

import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ProfilUtilisateurCourantService {

    private final ProfilUtilisateurRepository profilUtilisateurRepository;

    public ProfilUtilisateurCourantService(ProfilUtilisateurRepository profilUtilisateurRepository) {
        this.profilUtilisateurRepository = profilUtilisateurRepository;
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
}
