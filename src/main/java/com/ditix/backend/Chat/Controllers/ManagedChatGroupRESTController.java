package com.ditix.backend.Chat.Controllers;

import com.ditix.backend.Chat.DTO.ManagedChatGroupDTO;
import com.ditix.backend.Chat.DTO.ManagedChatGroupRequest;
import com.ditix.backend.Chat.Services.ManagedChatGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;

@RestController
@RequestMapping("/api/v1/admin/chat/groups")
public class ManagedChatGroupRESTController {

    private final ManagedChatGroupService managedChatGroupService;
    private final ProfilUtilisateurCourantService profilUtilisateurCourantService;

    public ManagedChatGroupRESTController(ManagedChatGroupService managedChatGroupService, ProfilUtilisateurCourantService profilUtilisateurCourantService) {
        this.managedChatGroupService = managedChatGroupService;
        this.profilUtilisateurCourantService = profilUtilisateurCourantService;
    }

    private String extractAndVerifyAdminUserId(JwtAuthenticationToken authentication) {
        ProfilUtilisateur profil = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        if (profil.getRole() != RoleUtilisateur.ADMIN) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé. Rôle métier ADMIN requis.");
        }
        return profil.getKeycloakId().toString();
    }

    @PostMapping("/global")
    public ResponseEntity<ManagedChatGroupDTO> createGlobalGroup(
            @RequestBody ManagedChatGroupRequest request,
            JwtAuthenticationToken authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ManagedChatGroupDTO(
                managedChatGroupService.createOrGetGlobalGroup(request.getName(), extractAndVerifyAdminUserId(authentication))));
    }

    @PostMapping("/cohort")
    public ResponseEntity<ManagedChatGroupDTO> createCohortGroup(
            @RequestBody ManagedChatGroupRequest request,
            JwtAuthenticationToken authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ManagedChatGroupDTO(
                managedChatGroupService.createOrGetCohortGroup(request.getReferenceId(), request.getName(), extractAndVerifyAdminUserId(authentication))));
    }

    @PostMapping("/structure")
    public ResponseEntity<ManagedChatGroupDTO> createStructureGroup(
            @RequestBody ManagedChatGroupRequest request,
            JwtAuthenticationToken authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ManagedChatGroupDTO(
                managedChatGroupService.createOrGetStructureGroup(request.getReferenceId(), request.getName(), extractAndVerifyAdminUserId(authentication))));
    }

    @GetMapping
    public ResponseEntity<List<ManagedChatGroupDTO>> listManagedGroups(JwtAuthenticationToken authentication) {
        extractAndVerifyAdminUserId(authentication);
        return ResponseEntity.ok(managedChatGroupService.listManagedGroups()
                .stream()
                .map(ManagedChatGroupDTO::new)
                .collect(Collectors.toList()));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ManagedChatGroupDTO> archiveGroup(
            @PathVariable Long id,
            JwtAuthenticationToken authentication) {
        return ResponseEntity.ok(new ManagedChatGroupDTO(
                managedChatGroupService.archiveGroup(id, extractAndVerifyAdminUserId(authentication))));
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<ManagedChatGroupDTO> reactivateGroup(
            @PathVariable Long id,
            JwtAuthenticationToken authentication) {
        return ResponseEntity.ok(new ManagedChatGroupDTO(
                managedChatGroupService.reactivateGroup(id, extractAndVerifyAdminUserId(authentication))));
    }
}
