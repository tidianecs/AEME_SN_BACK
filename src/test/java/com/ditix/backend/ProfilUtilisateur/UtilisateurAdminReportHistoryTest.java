package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.Report.DTO.ReportResponseDTO;
import com.ditix.backend.Report.Services.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UtilisateurAdminReportHistoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfilUtilisateurRepository profilRepository;

    @MockBean
    private ReportService reportService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private ProfilUtilisateur adminProfil;
    private ProfilUtilisateur targetProfil;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM reports");
        jdbcTemplate.execute("DELETE FROM meeting_participants");
        jdbcTemplate.execute("DELETE FROM meetings");
        jdbcTemplate.execute("DELETE FROM conversation_members");
        jdbcTemplate.execute("DELETE FROM conversations");
        profilRepository.deleteAll();

        adminProfil = new ProfilUtilisateur();
        adminProfil.setKeycloakId(UUID.randomUUID());
        adminProfil.setPrenom("Admin");
        adminProfil.setNom("Test");
        adminProfil.setEmail("admin@test.com");
        adminProfil.setRole(RoleUtilisateur.ADMIN);
        adminProfil.setActif(true);
        adminProfil = profilRepository.saveAndFlush(adminProfil);

        targetProfil = new ProfilUtilisateur();
        targetProfil.setKeycloakId(UUID.randomUUID());
        targetProfil.setPrenom("Target");
        targetProfil.setNom("User");
        targetProfil.setEmail("target@test.com");
        targetProfil.setRole(RoleUtilisateur.ADMIN);
        targetProfil.setActif(true);
        targetProfil = profilRepository.saveAndFlush(targetProfil);
    }

    @Test
    void testGetUserReports_Success_ActiveAdmin() throws Exception {
        com.ditix.backend.Report.Model.Report mockReport = mock(com.ditix.backend.Report.Model.Report.class);
        when(mockReport.getId()).thenReturn(1L);
        when(mockReport.getNomGestionnaire()).thenReturn("Target User");
        when(mockReport.getReportStatus()).thenReturn(com.ditix.backend.Report.Model.ReportStatus.SUBMITTED);
        ReportResponseDTO report = new ReportResponseDTO(mockReport);

        when(reportService.getReportsByUserId(targetProfil.getKeycloakId().toString()))
                .thenReturn(Collections.singletonList(report));

        mockMvc.perform(get("/api/v2/admin/utilisateurs/" + targetProfil.getId() + "/reports")
                        .with(jwt().jwt(builder -> builder.subject(adminProfil.getKeycloakId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nomGestionnaire").value("Target User"));

        verify(reportService, times(1)).getReportsByUserId(targetProfil.getKeycloakId().toString());
    }

    @Test
    void testGetUserReports_Success_EmptyHistory() throws Exception {
        when(reportService.getReportsByUserId(targetProfil.getKeycloakId().toString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v2/admin/utilisateurs/" + targetProfil.getId() + "/reports")
                        .with(jwt().jwt(builder -> builder.subject(adminProfil.getKeycloakId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetUserReports_NotFound_TargetMissing() throws Exception {
        mockMvc.perform(get("/api/v2/admin/utilisateurs/999999/reports")
                        .with(jwt().jwt(builder -> builder.subject(adminProfil.getKeycloakId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserReports_Forbidden_Dage() throws Exception {
        com.ditix.backend.Ministere.Model.Ministere min = new com.ditix.backend.Ministere.Model.Ministere();
        min.setNom("Test Min 2");
        min.setCode("TM2");
        min = ministereRepository.saveAndFlush(min);

        ProfilUtilisateur dage = new ProfilUtilisateur();
        dage.setKeycloakId(UUID.randomUUID());
        dage.setPrenom("Dage");
        dage.setNom("User");
        dage.setEmail("dage@test.com");
        dage.setRole(RoleUtilisateur.DAGE);
        dage.setMinistere(min);
        dage.setActif(true);
        ProfilUtilisateur savedDage = profilRepository.saveAndFlush(dage);

        mockMvc.perform(get("/api/v2/admin/utilisateurs/" + targetProfil.getId() + "/reports")
                        .with(jwt().jwt(builder -> builder.subject(savedDage.getKeycloakId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_admin"))) // Even with Keycloak ROLE_admin
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }



    @Test
    void testGetUserReports_Forbidden_InactiveAdmin() throws Exception {
        adminProfil.setActif(false);
        profilRepository.saveAndFlush(adminProfil);

        mockMvc.perform(get("/api/v2/admin/utilisateurs/" + targetProfil.getId() + "/reports")
                        .with(jwt().jwt(builder -> builder.subject(adminProfil.getKeycloakId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Autowired
    private com.ditix.backend.Ministere.Repository.MinistereRepository ministereRepository;

    @Test
    void testGetUserReports_Forbidden_KeycloakAdminButPostgresNonAdmin() throws Exception {
        com.ditix.backend.Ministere.Model.Ministere min = new com.ditix.backend.Ministere.Model.Ministere();
        min.setNom("Test Min");
        min.setCode("TM");
        min = ministereRepository.saveAndFlush(min);

        ProfilUtilisateur user = new ProfilUtilisateur();
        user.setKeycloakId(UUID.randomUUID());
        user.setPrenom("Fake");
        user.setNom("Admin");
        user.setEmail("fakeadmin@test.com");
        user.setRole(RoleUtilisateur.DAGE);
        user.setMinistere(min);
        user.setActif(true);
        ProfilUtilisateur savedUser = profilRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/v2/admin/utilisateurs/" + targetProfil.getId() + "/reports")
                        .with(jwt().jwt(builder -> builder.subject(savedUser.getKeycloakId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserReports_Unauthorized_NoJwt() throws Exception {
        mockMvc.perform(get("/api/v2/admin/utilisateurs/" + targetProfil.getId() + "/reports")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
