package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.ProfilUtilisateur.Controllers.UtilisateurAdminController;
import com.ditix.backend.ProfilUtilisateur.DTO.CreerUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.DTO.CreationUtilisateurResponse;
import com.ditix.backend.ProfilUtilisateur.DTO.ProfilUtilisateurDTO;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.ActivationUtilisateurOrchestrator;
import com.ditix.backend.ProfilUtilisateur.Services.CreationUtilisateurOrchestrator;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.ditix.backend.Core.SecurityConfig;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@WebMvcTest(controllers = UtilisateurAdminController.class)
@Import(SecurityConfig.class)
public class UtilisateurAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreationUtilisateurOrchestrator creationUtilisateurOrchestrator;

    @MockBean
    private ProfilUtilisateurCourantService profilUtilisateurCourantService;

    @MockBean
    private ActivationUtilisateurOrchestrator activationUtilisateurOrchestrator;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository profilUtilisateurRepository;

    @MockBean
    private com.ditix.backend.Report.Services.ReportService reportService;

    private CreerUtilisateurRequest request;
    private JwtAuthenticationToken mockToken;
    private ProfilUtilisateur profilAdmin;

    @BeforeEach
    void setUp() {
        request = new CreerUtilisateurRequest();
        request.setPrenom("Prenom");
        request.setNom("Nom");
        request.setEmail("test@aeme.sn");
        request.setRole(RoleUtilisateur.DAGE);
        request.setMinistereId(1L);

        mockToken = Mockito.mock(JwtAuthenticationToken.class);

        profilAdmin = new ProfilUtilisateur();
        profilAdmin.setRole(RoleUtilisateur.ADMIN);
    }

    @Test
    void creerUtilisateur_NonAuthentifie_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v2/admin/utilisateurs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void creerUtilisateur_AdminMetier_Success() throws Exception {
        when(profilUtilisateurCourantService.obtenirProfilCourant(any(JwtAuthenticationToken.class))).thenReturn(profilAdmin);

        ProfilUtilisateur createdProfil = new ProfilUtilisateur();
        createdProfil.setId(10L);
        createdProfil.setEmail("test@aeme.sn");
        createdProfil.setRole(RoleUtilisateur.DAGE);

        CreationUtilisateurResponse response = new CreationUtilisateurResponse(
                new ProfilUtilisateurDTO(createdProfil), true);
        when(creationUtilisateurOrchestrator.creerUtilisateur(any())).thenReturn(response);

        mockMvc.perform(post("/api/v2/admin/utilisateurs")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invitationEnvoyee").value(true))
                .andExpect(jsonPath("$.utilisateur.email").value("test@aeme.sn"))
                .andExpect(jsonPath("$.utilisateur.keycloakId").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.credential").doesNotExist());

        Mockito.verify(profilUtilisateurCourantService).obtenirProfilCourant(any(JwtAuthenticationToken.class));
        Mockito.verify(profilUtilisateurCourantService, Mockito.never()).obtenirProfilCourant(null);
    }

    @Test
    void creerUtilisateur_NonAdminMetier_Forbidden() throws Exception {
        ProfilUtilisateur profilUser = new ProfilUtilisateur();
        profilUser.setRole(RoleUtilisateur.GESTIONNAIRE);
        when(profilUtilisateurCourantService.obtenirProfilCourant(any(JwtAuthenticationToken.class))).thenReturn(profilUser);

        mockMvc.perform(post("/api/v2/admin/utilisateurs")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        Mockito.verify(profilUtilisateurCourantService).obtenirProfilCourant(any(JwtAuthenticationToken.class));
        Mockito.verify(profilUtilisateurCourantService, Mockito.never()).obtenirProfilCourant(null);
    }
}
