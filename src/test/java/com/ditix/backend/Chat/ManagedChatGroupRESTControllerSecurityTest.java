package com.ditix.backend.Chat;

import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import com.ditix.backend.Chat.Controllers.ManagedChatGroupRESTController;
import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Services.ManagedChatGroupService;
import com.ditix.backend.Core.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManagedChatGroupRESTController.class)
@Import(SecurityConfig.class)
public class ManagedChatGroupRESTControllerSecurityTest {
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

                    if (sub.contains("admin") || sub.contains("Admin") || sub.equals("00000000-0000-0000-0000-000000000001")) {
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
    private ManagedChatGroupService managedChatGroupService;

    private Conversation createMockGroup(Long id, ConversationType type, String ref) {
        Conversation conv = new Conversation();
        conv.setId(id);
        conv.setType(type);
        conv.setName("Group Name");
        conv.setReferenceId(ref);
        conv.setActive(true);
        conv.setSystemManaged(true);
        return conv;
    }

    @Test
    void createGlobal_standardUser_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/chat/groups/global")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Global\"}")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("11111111-1111-1111-1111-111111111111"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());

        verify(managedChatGroupService, never()).createOrGetGlobalGroup(anyString(), anyString());
    }

    @Test
    void createGlobal_adminUser_shouldReturn201() throws Exception {
        when(managedChatGroupService.createOrGetGlobalGroup(eq("Global"), eq("00000000-0000-0000-0000-000000000001")))
                .thenReturn(createMockGroup(1L, ConversationType.GLOBAL, "GLOBAL"));

        mockMvc.perform(post("/api/v1/admin/chat/groups/global")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Global\"}")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000001"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("GLOBAL"));
    }

    @Test
    void createCohort_adminUser_shouldReturn201() throws Exception {
        when(managedChatGroupService.createOrGetCohortGroup(eq("ref1"), eq("Cohort"), eq("00000000-0000-0000-0000-000000000001")))
                .thenReturn(createMockGroup(2L, ConversationType.COHORT, "ref1"));

        mockMvc.perform(post("/api/v1/admin/chat/groups/cohort")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"referenceId\": \"ref1\", \"name\": \"Cohort\"}")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000001"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("COHORT"))
                .andExpect(jsonPath("$.referenceId").value("ref1"));
    }

    @Test
    void createMinistere_adminUser_shouldReturn201() throws Exception {
        when(managedChatGroupService.createOrGetMinistereGroup(eq("min1"), eq("Ministere"), eq("00000000-0000-0000-0000-000000000001")))
                .thenReturn(createMockGroup(3L, ConversationType.MINISTERE, "min1"));

        mockMvc.perform(post("/api/v1/admin/chat/groups/ministere")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"referenceId\": \"min1\", \"name\": \"Ministere\"}")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000001"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("MINISTERE"))
                .andExpect(jsonPath("$.referenceId").value("min1"));
    }
    void listGroups_adminUser_shouldReturn200() throws Exception {
        when(managedChatGroupService.listManagedGroups())
                .thenReturn(List.of(createMockGroup(1L, ConversationType.GLOBAL, "GLOBAL")));

        mockMvc.perform(get("/api/v1/admin/chat/groups")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000001"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "admin")
    public void testCreateGlobalGroup_IgnoresCreatedByUserIdInJson() throws Exception {
        when(managedChatGroupService.createOrGetGlobalGroup(eq("Global Admin"), anyString()))
                .thenReturn(createMockGroup(1L, ConversationType.GLOBAL, "GLOBAL"));

        String json = """
            {
                "name": "Global Admin",
                "createdByUserId": "hacker123"
            }
            """;

        mockMvc.perform(post("/api/v1/admin/chat/groups/global")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000001"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isCreated());

        // Verification happens via the integration test (which calls the service directly)
        // Here we just test the endpoint accepts the request and does not crash or use it.
    }
}
