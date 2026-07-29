package com.ditix.backend.Auth;

import com.ditix.backend.Auth.Controllers.AuthController;
import com.ditix.backend.Auth.Services.AuthService;
import com.ditix.backend.Report.Services.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private ReportService reportService;

    @Test
    void getUserById_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/users/user-a"))
                .andExpect(status().isUnauthorized());

        verify(authService, never()).getUserById(anyString());
    }

    @Test
    void getUserById_owner_shouldReturn200() throws Exception {
        when(authService.getUserById("user-a")).thenReturn(Map.of("id", "user-a"));

        mockMvc.perform(get("/api/v1/auth/users/user-a")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("user-a"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk());

        verify(authService, times(1)).getUserById("user-a");
    }

    @Test
    void getUserById_otherUser_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/auth/users/user-b")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("user-a"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());

        verify(authService, never()).getUserById(anyString());
    }

    @Test
    void getUserById_admin_shouldReturn200() throws Exception {
        when(authService.getUserById("user-b")).thenReturn(Map.of("id", "user-b"));

        mockMvc.perform(get("/api/v1/auth/users/user-b")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("admin-a"))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());

        verify(authService, times(1)).getUserById("user-b");
    }

    @Test
    void getUserById_nonexistentTargetByStandardUser_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/auth/users/nonexistent-user")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("user-a"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());

        verify(authService, never()).getUserById(anyString());
    }

    @Test
    void getUserById_blankSubject_shouldReturn403() throws Exception {
        // Jwt building might not allow blank subject, we can try
        mockMvc.perform(get("/api/v1/auth/users/user-b")
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(jwt -> jwt.subject("   "))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());

        verify(authService, never()).getUserById(anyString());
    }
}
