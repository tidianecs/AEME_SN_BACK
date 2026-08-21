package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.ProfilUtilisateur.Controllers.UtilisateurAdminController;
import com.ditix.backend.ProfilUtilisateur.DTO.ActivationUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.ActivationUtilisateurOrchestrator;
import com.ditix.backend.ProfilUtilisateur.Services.CreationUtilisateurOrchestrator;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.ditix.backend.ProfilUtilisateur.Services.GestionCompteKeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ditix.backend.Core.SecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(UtilisateurAdminController.class)
@Import(SecurityConfig.class)
class UtilisateurAdminActivationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GestionCompteKeycloakService gestionCompteKeycloakService;

    @MockBean
    private CreationUtilisateurOrchestrator creationUtilisateurOrchestrator;

    @MockBean
    private ProfilUtilisateurCourantService profilUtilisateurCourantService;

    @MockBean
    private ActivationUtilisateurOrchestrator activationUtilisateurOrchestrator;

    @MockBean
    private com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository profilUtilisateurRepository;

    @MockBean
    private com.ditix.backend.Report.Services.ReportService reportService;

    @Test
    void updateActivation_noJwt_shouldReturn401() throws Exception {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(false);

        mockMvc.perform(patch("/api/v2/admin/utilisateurs/1/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateActivation_withRoleUser_shouldReturn403() throws Exception {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(false);

        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setRole(RoleUtilisateur.GESTIONNAIRE);
        when(profilUtilisateurCourantService.obtenirProfilCourant(any())).thenReturn(profil);

        mockMvc.perform(patch("/api/v2/admin/utilisateurs/1/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateActivation_withRoleAdminButPgNotAdmin_shouldReturn403() throws Exception {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(false);

        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setRole(RoleUtilisateur.GESTIONNAIRE);
        when(profilUtilisateurCourantService.obtenirProfilCourant(any())).thenReturn(profil);

        mockMvc.perform(patch("/api/v2/admin/utilisateurs/1/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateActivation_withRoleAdminButPgDage_shouldReturn403() throws Exception {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(false);

        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setRole(RoleUtilisateur.DAGE);
        when(profilUtilisateurCourantService.obtenirProfilCourant(any())).thenReturn(profil);

        mockMvc.perform(patch("/api/v2/admin/utilisateurs/1/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateActivation_actorAbsent_shouldReturn403() throws Exception {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(false);

        when(profilUtilisateurCourantService.obtenirProfilCourant(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(patch("/api/v2/admin/utilisateurs/1/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateActivation_actorInactive_shouldReturn403() throws Exception {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(false);

        when(profilUtilisateurCourantService.obtenirProfilCourant(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(patch("/api/v2/admin/utilisateurs/1/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateActivation_nullActif_shouldReturn400() throws Exception {
        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setRole(RoleUtilisateur.ADMIN);
        when(profilUtilisateurCourantService.obtenirProfilCourant(any())).thenReturn(profil);

        mockMvc.perform(patch("/api/v2/admin/utilisateurs/1/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateActivation_validRequest_shouldReturn200() throws Exception {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(false);

        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setRole(RoleUtilisateur.ADMIN);
        when(profilUtilisateurCourantService.obtenirProfilCourant(any())).thenReturn(profil);

        mockMvc.perform(patch("/api/v2/admin/utilisateurs/1/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());

        verify(activationUtilisateurOrchestrator, times(1)).activerUtilisateur(eq(1L), any(), eq(profil));
    }

    @Test
    void updateActivation_selfDeactivation_shouldReturn403() throws Exception {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(false);

        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setId(1L);
        profil.setRole(RoleUtilisateur.ADMIN);
        when(profilUtilisateurCourantService.obtenirProfilCourant(any())).thenReturn(profil);

        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Impossible de modifier son propre statut d'activation"))
            .when(activationUtilisateurOrchestrator).activerUtilisateur(eq(1L), any(), eq(profil));

        mockMvc.perform(patch("/api/v2/admin/utilisateurs/1/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isForbidden());
    }
}
