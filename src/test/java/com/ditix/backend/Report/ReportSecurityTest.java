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
}
