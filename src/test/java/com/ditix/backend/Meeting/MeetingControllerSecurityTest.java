package com.ditix.backend.Meeting;

import com.ditix.backend.Core.SecurityConfig;
import com.ditix.backend.Meeting.Controllers.MeetingController;
import com.ditix.backend.Meeting.Services.MeetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeetingController.class)
@Import(SecurityConfig.class)
public class MeetingControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MeetingService meetingService;

    @MockBean
    private com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService profilService;

    @Test
    void createMeeting_noJwt_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createMeeting_profileAbsent_shouldReturn403() throws Exception {
        when(profilService.obtenirProfilCourant(any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(post("/api/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMeeting_profileInactive_shouldReturn403() throws Exception {
        when(profilService.obtenirProfilCourant(any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(post("/api/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMeeting_activeAdmin_shouldBeAllowed() throws Exception {
        com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur profil = new com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur();
        profil.setKeycloakId(java.util.UUID.randomUUID());
        profil.setRole(com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur.ADMIN);
        profil.setActif(true);
        when(profilService.obtenirProfilCourant(any())).thenReturn(profil);

        mockMvc.perform(post("/api/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject("RAW-JWT-SUBJECT-123")).authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isCreated());

        verify(meetingService, times(1)).createMeeting(any(), eq(profil.getKeycloakId().toString()));
    }

    @Test
    void createMeeting_activeDage_shouldReturn403() throws Exception {
        com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur profil = new com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur();
        profil.setKeycloakId(java.util.UUID.randomUUID());
        profil.setRole(com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur.DAGE);
        profil.setActif(true);
        when(profilService.obtenirProfilCourant(any())).thenReturn(profil);

        mockMvc.perform(post("/api/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMeeting_activeGestionnaire_shouldReturn403() throws Exception {
        com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur profil = new com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur();
        profil.setKeycloakId(java.util.UUID.randomUUID());
        profil.setRole(com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur.GESTIONNAIRE);
        profil.setActif(true);
        when(profilService.obtenirProfilCourant(any())).thenReturn(profil);

        mockMvc.perform(post("/api/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMeeting_inactiveAdmin_shouldReturn403() throws Exception {
        com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur profil = new com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur();
        profil.setKeycloakId(java.util.UUID.randomUUID());
        profil.setRole(com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur.ADMIN);
        profil.setActif(false);
        when(profilService.obtenirProfilCourant(any())).thenReturn(profil);

        mockMvc.perform(post("/api/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void patchMeeting_profileAbsent_shouldReturn403() throws Exception {
        when(profilService.obtenirProfilCourant(any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(patch("/api/v1/meetings/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"TERMINE\"}")
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void patchMeeting_profileInactive_shouldReturn403() throws Exception {
        when(profilService.obtenirProfilCourant(any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(patch("/api/v1/meetings/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"TERMINE\"}")
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteMeeting_profileAbsent_shouldReturn403() throws Exception {
        when(profilService.obtenirProfilCourant(any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(delete("/api/v1/meetings/1")
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteMeeting_profileInactive_shouldReturn403() throws Exception {
        when(profilService.obtenirProfilCourant(any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(delete("/api/v1/meetings/1")
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }
}
