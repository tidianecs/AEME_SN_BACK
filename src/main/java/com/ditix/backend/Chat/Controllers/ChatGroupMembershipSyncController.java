package com.ditix.backend.Chat.Controllers;

import com.ditix.backend.Chat.DTO.ChatGroupSyncReport;
import com.ditix.backend.Chat.Services.ChatGroupMembershipSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/chat/groups")
public class ChatGroupMembershipSyncController {

    private final ChatGroupMembershipSyncService syncService;
    private final ProfilUtilisateurCourantService profilUtilisateurCourantService;

    public ChatGroupMembershipSyncController(ChatGroupMembershipSyncService syncService, ProfilUtilisateurCourantService profilUtilisateurCourantService) {
        this.syncService = syncService;
        this.profilUtilisateurCourantService = profilUtilisateurCourantService;
    }

    private void extractAndVerifyAdminUserId(JwtAuthenticationToken authentication) {
        ProfilUtilisateur profil = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        if (profil.getRole() != RoleUtilisateur.ADMIN) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé. Rôle métier ADMIN requis.");
        }
    }

    @PostMapping("/{id:\\d+}/sync-members")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ChatGroupSyncReport> syncGroupMembers(@PathVariable Long id, JwtAuthenticationToken authentication) {
        extractAndVerifyAdminUserId(authentication);
        ChatGroupSyncReport report = syncService.syncGroup(id);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/sync-members")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<ChatGroupSyncReport>> syncAllGroupMembers(JwtAuthenticationToken authentication) {
        extractAndVerifyAdminUserId(authentication);
        List<ChatGroupSyncReport> reports = syncService.syncAllActiveManagedGroups();
        return ResponseEntity.ok(reports);
    }
}
