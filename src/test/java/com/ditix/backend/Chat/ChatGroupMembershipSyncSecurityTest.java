package com.ditix.backend.Chat;

import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import com.ditix.backend.Chat.Controllers.ChatGroupMembershipSyncController;
import com.ditix.backend.Chat.DTO.ChatGroupSyncReport;
import com.ditix.backend.Chat.Services.ChatGroupMembershipSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatGroupMembershipSyncController.class)
@Import(com.ditix.backend.Core.SecurityConfig.class)
public class ChatGroupMembershipSyncSecurityTest {
    @org.junit.jupiter.api.BeforeEach
    void setUp() {

        try {
            org.mockito.Mockito.lenient().when(profilUtilisateurCourantService.obtenirProfilCourant(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
                Object arg = invocation.getArgument(0);
                if (arg instanceof JwtAuthenticationToken) {
                    JwtAuthenticationToken auth = (JwtAuthenticationToken) arg;
                    String sub = auth.getToken().getSubject();
                    if (sub == null || sub.trim().isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
                    }
                    ProfilUtilisateur profil = new ProfilUtilisateur();

                    // Use a standard UUID if it's not a UUID format to prevent parsing errors elsewhere
                    try {
                        profil.setKeycloakId(UUID.fromString(sub));
                    } catch (Exception e) {
                        // fallback for tests using dummy strings like "11111111-1111-1111-1111-111111111111"
                        if ("00000000-0000-0000-0000-000000000001".equals(sub)) {
                             profil.setKeycloakId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
                        } else {
                             profil.setKeycloakId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
                        }
                    }

                    profil.setActif(true);

                    if (sub.contains("admin") || sub.contains("Admin")) {
                        profil.setRole(RoleUtilisateur.ADMIN);
                    } else if (sub.contains("dage")) {
                        profil.setRole(RoleUtilisateur.DAGE);
                    } else if (sub.contains("gestionnaire")) {
                        profil.setRole(RoleUtilisateur.GESTIONNAIRE);
                    } else {
                        profil.setRole(RoleUtilisateur.GESTIONNAIRE);
                    }

                    if (sub.contains("inactive")) {
                        profil.setActif(false);
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
                    }

                    if (sub.contains("missing_profile")) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
                    }

                    return profil;
                }
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
            });
        } catch (Exception e) {}
    }

    @MockBean
    private ProfilUtilisateurCourantService profilUtilisateurCourantService;


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatGroupMembershipSyncService syncService;

    @Tes
    void testSyncGroup_NoToken_Returns401() throws Exception {
        mockMvc.perform(post("/api/v1/admin/chat/groups/1/sync-members")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Tes
    @WithMockUser(roles = "user")
    void testSyncGroup_StandardUser_Returns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/chat/groups/1/sync-members")
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("11111111-1111-1111-1111-111111111111"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Tes
    @WithMockUser(roles = "admin")
    void testSyncGroup_AdminUser_Returns200() throws Exception {
        when(syncService.syncGroup(anyLong())).thenReturn(new ChatGroupSyncReport());

        mockMvc.perform(post("/api/v1/admin/chat/groups/1/sync-members")
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("admin1"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
    }

    @Tes
    @WithMockUser(roles = "admin")
    void testSyncAllGroups_AdminUser_Returns200() throws Exception {
        when(syncService.syncAllActiveManagedGroups()).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/admin/chat/groups/sync-members")
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("admin1"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
    }
}
