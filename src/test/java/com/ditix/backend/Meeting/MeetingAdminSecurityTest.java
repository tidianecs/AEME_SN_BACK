package com.ditix.backend.Meeting;

import com.ditix.backend.Meeting.Controllers.MeetingAdminController;
import com.ditix.backend.Meeting.Services.MeetingAdminService;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MeetingAdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfilUtilisateurCourantService profilService;

    @MockBean
    private MeetingAdminService meetingAdminService;

    private ProfilUtilisateur adminProfil;
    private ProfilUtilisateur dageProfil;
    private ProfilUtilisateur gestionnaireProfil;

    @BeforeEach
    public void setup() {
        adminProfil = new ProfilUtilisateur();
        adminProfil.setRole(RoleUtilisateur.ADMIN);
        adminProfil.setActif(true);

        dageProfil = new ProfilUtilisateur();
        dageProfil.setRole(RoleUtilisateur.DAGE);
        dageProfil.setActif(true);

        gestionnaireProfil = new ProfilUtilisateur();
        gestionnaireProfil.setRole(RoleUtilisateur.GESTIONNAIRE);
        gestionnaireProfil.setActif(true);
    }

    @Test
    @WithAnonymousUser
    public void testNoJwt() throws Exception {
        mockMvc.perform(post("/api/v2/admin/meetings/global")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRoleUser() throws Exception {
        mockMvc.perform(post("/api/v2/admin/meetings/global")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_user")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testKeycloakAdminPostgresAdmin() throws Exception {
        Mockito.when(profilService.obtenirProfilCourant(Mockito.any())).thenReturn(adminProfil);

        mockMvc.perform(post("/api/v2/admin/meetings/global")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void testKeycloakAdminPostgresDage() throws Exception {
        Mockito.when(profilService.obtenirProfilCourant(Mockito.any())).thenReturn(dageProfil);

        mockMvc.perform(post("/api/v2/admin/meetings/global")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testKeycloakAdminPostgresGestionnaire() throws Exception {
        Mockito.when(profilService.obtenirProfilCourant(Mockito.any())).thenReturn(gestionnaireProfil);

        mockMvc.perform(post("/api/v2/admin/meetings/global")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testProfileAbsent() throws Exception {
        Mockito.when(profilService.obtenirProfilCourant(Mockito.any()))
               .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(post("/api/v2/admin/meetings/global")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testProfileInactive() throws Exception {
        adminProfil.setActif(false);
        Mockito.when(profilService.obtenirProfilCourant(Mockito.any()))
               .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(post("/api/v2/admin/meetings/global")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }
}
