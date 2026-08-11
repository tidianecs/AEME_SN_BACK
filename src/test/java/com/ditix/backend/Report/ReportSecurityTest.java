package com.ditix.backend.Report;

import com.ditix.backend.Core.SecurityConfig;
import com.ditix.backend.Report.Controllers.ReportController;
import com.ditix.backend.Report.Services.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
public class ReportSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService profilUtilisateurCourantService;

    @Test
    void getAllReports_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/all"))
                .andExpect(status().isUnauthorized());

        verify(reportService, never()).getAllReports(any());
        verify(profilUtilisateurCourantService, never()).obtenirProfilCourant(any());
    }

    @Test
    void getAllReports_withRoleAdmin_shouldBeAllowed() throws Exception {
        com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur profilAdmin = new com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur();
        profilAdmin.setRole(com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur.ADMIN);
        profilAdmin.setActif(true);

        when(profilUtilisateurCourantService.obtenirProfilCourant(any())).thenReturn(profilAdmin);
        when(reportService.getAllReports(profilAdmin)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/reports/all")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());

        verify(profilUtilisateurCourantService, times(1)).obtenirProfilCourant(any());
        verify(reportService, times(1)).getAllReports(profilAdmin);
    }

    @Test
    void getAllReports_withRoleUser_profilDage_shouldBeAllowed() throws Exception {
        com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur profilDage = new com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur();
        profilDage.setRole(com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur.DAGE);
        profilDage.setActif(true);

        when(profilUtilisateurCourantService.obtenirProfilCourant(any())).thenReturn(profilDage);
        when(reportService.getAllReports(profilDage)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/reports/all")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk());

        verify(profilUtilisateurCourantService, times(1)).obtenirProfilCourant(any());
        verify(reportService, times(1)).getAllReports(profilDage);
    }

    @Test
    void getAllReports_withRoleUser_profilGestionnaire_shouldBeAllowed() throws Exception {
        com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur profilGestionnaire = new com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur();
        profilGestionnaire.setRole(com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur.GESTIONNAIRE);
        profilGestionnaire.setActif(true);

        when(profilUtilisateurCourantService.obtenirProfilCourant(any())).thenReturn(profilGestionnaire);
        when(reportService.getAllReports(profilGestionnaire)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/reports/all")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk());

        verify(profilUtilisateurCourantService, times(1)).obtenirProfilCourant(any());
        verify(reportService, times(1)).getAllReports(profilGestionnaire);
    }

    @Test
    void getAllReports_withRoleUser_profilAbsent_shouldReturn403() throws Exception {
        when(profilUtilisateurCourantService.obtenirProfilCourant(any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Profil non trouvé"));

        mockMvc.perform(get("/api/v1/reports/all")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());

        verify(profilUtilisateurCourantService, times(1)).obtenirProfilCourant(any());
        verify(reportService, never()).getAllReports(any());
    }

    @Test
    void getAllReports_withRoleUser_profilInactive_shouldReturn403() throws Exception {
        when(profilUtilisateurCourantService.obtenirProfilCourant(any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Profil inactif"));

        mockMvc.perform(get("/api/v1/reports/all")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());

        verify(profilUtilisateurCourantService, times(1)).obtenirProfilCourant(any());
        verify(reportService, never()).getAllReports(any());
    }

    @Test
    void getMyReports_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/reports"))
                .andExpect(status().isUnauthorized());

        verify(reportService, never()).getMyReports(anyString());
    }

    @Test
    void getMyReports_withRoleUser_shouldBeAllowed() throws Exception {
        when(reportService.getMyReports(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/reports")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk());

        verify(reportService, times(1)).getMyReports(anyString());
    }

    @Test
    void getReportById_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/1"))
                .andExpect(status().isUnauthorized());
        verify(reportService, never()).getReportById(anyLong(), any());
    }

    @Test
    void getReportById_owner_shouldBeAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/reports/1")
                .with(jwt().jwt(jwt -> jwt.subject("owner-user-id")).authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk());
        verify(reportService, times(1)).getReportById(eq(1L), any());
    }

    @Test
    void getReportById_admin_shouldBeAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/reports/1")
                .with(jwt().jwt(jwt -> jwt.subject("admin-user-id")).authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
        verify(reportService, times(1)).getReportById(eq(1L), any());
    }

    @Test
    void getReportById_serviceThrows403_shouldReturn403() throws Exception {
        when(reportService.getReportById(eq(1L), any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));
        mockMvc.perform(get("/api/v1/reports/1")
                .with(jwt().jwt(jwt -> jwt.subject("other-user-id")).authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReportById_serviceThrows404_shouldReturn404() throws Exception {
        when(reportService.getReportById(eq(1L), any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        mockMvc.perform(get("/api/v1/reports/1")
                .with(jwt().jwt(jwt -> jwt.subject("owner-user-id")).authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadFile_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/1/download/illustrations"))
                .andExpect(status().isUnauthorized());
        verify(reportService, never()).getRawReport(anyLong(), any());
    }

    @Test
    void downloadFile_owner_shouldBeAllowed() throws Exception {
        com.ditix.backend.Report.Model.Report report = new com.ditix.backend.Report.Model.Report();
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-illustrations", ".png");
        report.setIllustrationsPath(tempFile.toString());
        report.setIllustrationsName("dummy.png");
        when(reportService.getRawReport(eq(1L), any())).thenReturn(report);

        try {
            mockMvc.perform(get("/api/v1/reports/1/download/illustrations")
                    .with(jwt().jwt(jwt -> jwt.subject("owner-user-id")).authorities(new SimpleGrantedAuthority("ROLE_user"))))
                    .andExpect(status().isOk());
        } finally {
            java.nio.file.Files.deleteIfExists(tempFile);
        }

        verify(reportService, times(1)).getRawReport(eq(1L), any());
    }

    @Test
    void downloadFile_serviceThrows403_shouldReturn403() throws Exception {
        when(reportService.getRawReport(eq(1L), any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));
        mockMvc.perform(get("/api/v1/reports/1/download/illustrations")
                .with(jwt().jwt(jwt -> jwt.subject("other-user-id")).authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void downloadFile_serviceThrows404_shouldReturn404() throws Exception {
        when(reportService.getRawReport(eq(1L), any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        mockMvc.perform(get("/api/v1/reports/1/download/illustrations")
                .with(jwt().jwt(jwt -> jwt.subject("owner-user-id")).authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadFile_admin_shouldBeAllowed() throws Exception {
        com.ditix.backend.Report.Model.Report report = new com.ditix.backend.Report.Model.Report();
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-illustrations-admin", ".png");
        report.setIllustrationsPath(tempFile.toString());
        report.setIllustrationsName("dummy_admin.png");
        when(reportService.getRawReport(eq(1L), any())).thenReturn(report);

        try {
            mockMvc.perform(get("/api/v1/reports/1/download/illustrations")
                    .with(jwt().jwt(jwt -> jwt.subject("admin-user-id")).authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                    .andExpect(status().isOk());
        } finally {
            java.nio.file.Files.deleteIfExists(tempFile);
        }

        verify(reportService, times(1)).getRawReport(eq(1L), any());
    }

    @Test
    void deleteReport_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/v1/reports/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteReport_profileAbsent_shouldReturn403() throws Exception {
        when(profilUtilisateurCourantService.obtenirProfilCourant(any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(delete("/api/v1/reports/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteReport_profileInactive_shouldReturn403() throws Exception {
        when(profilUtilisateurCourantService.obtenirProfilCourant(any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));

        mockMvc.perform(delete("/api/v1/reports/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteReport_owner_shouldBeAllowed() throws Exception {
        com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur profil = new com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur();
        profil.setKeycloakId(java.util.UUID.randomUUID());
        when(profilUtilisateurCourantService.obtenirProfilCourant(any())).thenReturn(profil);

        mockMvc.perform(delete("/api/v1/reports/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk());
    }

    @Test
    void deleteReport_nonOwner_shouldReturn403() throws Exception {
        com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur profil = new com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur();
        profil.setKeycloakId(java.util.UUID.randomUUID());
        when(profilUtilisateurCourantService.obtenirProfilCourant(any())).thenReturn(profil);

        doThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN))
                .when(reportService).deleteReport(eq(1L), anyString());

        mockMvc.perform(delete("/api/v1/reports/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }
}
