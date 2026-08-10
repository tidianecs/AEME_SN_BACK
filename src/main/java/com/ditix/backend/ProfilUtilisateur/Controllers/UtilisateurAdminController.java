package com.ditix.backend.ProfilUtilisateur.Controllers;

import com.ditix.backend.ProfilUtilisateur.DTO.CreerUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.DTO.CreationUtilisateurResponse;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.CreationUtilisateurOrchestrator;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@RestController
@RequestMapping("/api/v2/admin/utilisateurs")
public class UtilisateurAdminController {

    private final CreationUtilisateurOrchestrator creationUtilisateurOrchestrator;
    private final ProfilUtilisateurCourantService profilUtilisateurCourantService;

    public UtilisateurAdminController(
            CreationUtilisateurOrchestrator creationUtilisateurOrchestrator,
            ProfilUtilisateurCourantService profilUtilisateurCourantService) {
        this.creationUtilisateurOrchestrator = creationUtilisateurOrchestrator;
        this.profilUtilisateurCourantService = profilUtilisateurCourantService;
    }

    @PostMapping
    public ResponseEntity<CreationUtilisateurResponse> creerUtilisateur(
            @Valid @RequestBody CreerUtilisateurRequest request,
            JwtAuthenticationToken authentication) {

        ProfilUtilisateur adminCourant = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        if (adminCourant.getRole() != RoleUtilisateur.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé. Rôle métier ADMIN requis.");
        }

        CreationUtilisateurResponse response = creationUtilisateurOrchestrator.creerUtilisateur(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
