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

    @Test
    void getAllReports_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/all"))
                .andExpect(status().isUnauthorized());

        verify(reportService, never()).getAllReports();
    }

    @Test
    void getAllReports_withRoleUser_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/reports/all")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());

        verify(reportService, never()).getAllReports();
    }

    @Test
    void getAllReports_withRoleAdmin_shouldBeAllowed() throws Exception {
        when(reportService.getAllReports()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/reports/all")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());

        verify(reportService, times(1)).getAllReports();
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
        verify(reportService, never()).getReportById(anyLong(), anyString(), anyBoolean());
    }

    @Test
    void getReportById_owner_shouldBeAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/reports/1")
                .with(jwt().jwt(jwt -> jwt.subject("owner-user-id")).authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk());
        verify(reportService, times(1)).getReportById(1L, "owner-user-id", false);
    }

    @Test
    void getReportById_admin_shouldBeAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/reports/1")
                .with(jwt().jwt(jwt -> jwt.subject("admin-user-id")).authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
        verify(reportService, times(1)).getReportById(1L, "admin-user-id", true);
    }

    @Test
    void getReportById_serviceThrows403_shouldReturn403() throws Exception {
        when(reportService.getReportById(eq(1L), anyString(), anyBoolean()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));
        mockMvc.perform(get("/api/v1/reports/1")
                .with(jwt().jwt(jwt -> jwt.subject("other-user-id")).authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReportById_serviceThrows404_shouldReturn404() throws Exception {
        when(reportService.getReportById(eq(1L), anyString(), anyBoolean()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        mockMvc.perform(get("/api/v1/reports/1")
                .with(jwt().jwt(jwt -> jwt.subject("owner-user-id")).authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadFile_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/1/download/illustrations"))
                .andExpect(status().isUnauthorized());
        verify(reportService, never()).getRawReport(anyLong(), anyString(), anyBoolean());
    }

    @Test
    void downloadFile_owner_shouldBeAllowed() throws Exception {
        com.ditix.backend.Report.Model.Report report = new com.ditix.backend.Report.Model.Report();
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-illustrations", ".png");
        report.setIllustrationsPath(tempFile.toString());
        report.setIllustrationsName("dummy.png");
        when(reportService.getRawReport(1L, "owner-user-id", false)).thenReturn(report);

        try {
            mockMvc.perform(get("/api/v1/reports/1/download/illustrations")
                    .with(jwt().jwt(jwt -> jwt.subject("owner-user-id")).authorities(new SimpleGrantedAuthority("ROLE_user"))))
                    .andExpect(status().isOk());
        } finally {
            java.nio.file.Files.deleteIfExists(tempFile);
        }

        verify(reportService, times(1)).getRawReport(1L, "owner-user-id", false);
    }

    @Test
    void downloadFile_serviceThrows403_shouldReturn403() throws Exception {
        when(reportService.getRawReport(eq(1L), anyString(), anyBoolean()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));
        mockMvc.perform(get("/api/v1/reports/1/download/illustrations")
                .with(jwt().jwt(jwt -> jwt.subject("other-user-id")).authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void downloadFile_serviceThrows404_shouldReturn404() throws Exception {
        when(reportService.getRawReport(eq(1L), anyString(), anyBoolean()))
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
        when(reportService.getRawReport(1L, "admin-user-id", true)).thenReturn(report);

        try {
            mockMvc.perform(get("/api/v1/reports/1/download/illustrations")
                    .with(jwt().jwt(jwt -> jwt.subject("admin-user-id")).authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                    .andExpect(status().isOk());
        } finally {
            java.nio.file.Files.deleteIfExists(tempFile);
        }

        verify(reportService, times(1)).getRawReport(1L, "admin-user-id", true);
    }
}
