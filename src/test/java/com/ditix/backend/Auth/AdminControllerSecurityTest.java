package com.ditix.backend.Auth;

import com.ditix.backend.Auth.Controllers.AdminController;
import com.ditix.backend.Auth.Services.AuthService;
import com.ditix.backend.Core.SecurityConfig;
import com.ditix.backend.Report.Services.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
public class AdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private ReportService reportService;

    @Test
    void legacyAdminCreateUser_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\"}")
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResolvedException() instanceof org.springframework.web.servlet.resource.NoResourceFoundException ||
                        result.getResolvedException() instanceof org.springframework.web.HttpRequestMethodNotSupportedException
                ));

        verify(authService, never()).inviteUser(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void legacyAdminUpdateMembership_shouldReturn404() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/users/123/membership")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"membershipService\":\"test\"}")
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResolvedException() instanceof org.springframework.web.servlet.resource.NoResourceFoundException ||
                        result.getResolvedException() instanceof org.springframework.web.HttpRequestMethodNotSupportedException
                ));

        verify(authService, never()).updateMembershipService(anyString(), anyString());
    }

    @Test
    void legacyAdminDeleteUser_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/123")
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResolvedException() instanceof org.springframework.web.servlet.resource.NoResourceFoundException ||
                        result.getResolvedException() instanceof org.springframework.web.HttpRequestMethodNotSupportedException
                ));

        verify(authService, never()).deleteUser(anyString());
    }
}
