package com.ditix.backend.ProfilUtilisateur.Controllers;
import com.ditix.backend.ProfilUtilisateur.DTO.CreerUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.DTO.CreationUtilisateurResponse;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.CreationUtilisateurOrchestrator;
import com.ditix.backend.ProfilUtilisateur.Services.GestionCompteKeycloakService;
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
import com.ditix.backend.ProfilUtilisateur.DTO.ActivationUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.Services.ActivationUtilisateurOrchestrator;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.ditix.backend.Report.DTO.ReportResponseDTO;
import com.ditix.backend.Report.Services.ReportService;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
@RestController
@RequestMapping("/api/v2/admin/utilisateurs")
public class UtilisateurAdminController {
    private final CreationUtilisateurOrchestrator creationUtilisateurOrchestrator;
    private final ProfilUtilisateurCourantService profilUtilisateurCourantService;
    private final ActivationUtilisateurOrchestrator activationUtilisateurOrchestrator;
    private final ProfilUtilisateurRepository profilUtilisateurRepository;
    private final ReportService reportService;
    private final GestionCompteKeycloakService gestionCompteKeycloakService;
    public UtilisateurAdminController(
            CreationUtilisateurOrchestrator creationUtilisateurOrchestrator,
            ProfilUtilisateurCourantService profilUtilisateurCourantService,
            ActivationUtilisateurOrchestrator activationUtilisateurOrchestrator,
            ProfilUtilisateurRepository profilUtilisateurRepository,
            ReportService reportService,
            GestionCompteKeycloakService gestionCompteKeycloakService) {
        this.creationUtilisateurOrchestrator = creationUtilisateurOrchestrator;
        this.profilUtilisateurCourantService = profilUtilisateurCourantService;
        this.activationUtilisateurOrchestrator = activationUtilisateurOrchestrator;
        this.profilUtilisateurRepository = profilUtilisateurRepository;
        this.reportService = reportService;
        this.gestionCompteKeycloakService = gestionCompteKeycloakService;
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
    @PatchMapping("/{id}/activation")
    public ResponseEntity<Void> updateActivation(
            @PathVariable Long id,
            @Valid @RequestBody ActivationUtilisateurRequest request,
            JwtAuthenticationToken authentication) {
        ProfilUtilisateur adminCourant = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        if (adminCourant.getRole() != RoleUtilisateur.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé. Rôle métier ADMIN requis.");
        }
        if (request.getActif() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le champ actif est obligatoire");
        }
        activationUtilisateurOrchestrator.activerUtilisateur(id, request, adminCourant);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{id}/resend-invitation")
    public ResponseEntity<Void> resendInvitation(
            @PathVariable Long id,
            JwtAuthenticationToken authentication) {
        ProfilUtilisateur adminCourant = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        if (adminCourant.getRole() != RoleUtilisateur.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé. Rôle métier ADMIN requis.");
        }
        ProfilUtilisateur target = profilUtilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur cible introuvable"));
        if (target.getKeycloakId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'utilisateur cible n'a pas d'identifiant Keycloak valide");
        }
        gestionCompteKeycloakService.resendInvitation(target.getKeycloakId());
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{id}/reports")
    public ResponseEntity<List<ReportResponseDTO>> getUserReports(
            @PathVariable Long id,
            JwtAuthenticationToken authentication) {
        ProfilUtilisateur adminCourant = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        if (adminCourant.getRole() != RoleUtilisateur.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé. Rôle métier ADMIN requis.");
        }
        ProfilUtilisateur target = profilUtilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur cible introuvable"));
        if (target.getKeycloakId() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "L'utilisateur cible n'a pas d'identifiant Keycloak valide");
        }
        List<ReportResponseDTO> reports = reportService.getReportsByUserId(target.getKeycloakId().toString());
        return ResponseEntity.ok(reports);
    }
}
