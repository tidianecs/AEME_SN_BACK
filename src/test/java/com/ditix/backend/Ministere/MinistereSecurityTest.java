package com.ditix.backend.Ministere;

import com.ditix.backend.Core.SecurityConfig;
import com.ditix.backend.Ministere.Controllers.MinistereController;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Services.MinistereService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MinistereController.class)
@Import(SecurityConfig.class)
public class MinistereSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MinistereService ministereService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllMinisteres_withoutAuth_shouldReturn200() throws Exception {
        when(ministereService.getAllMinisteres()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/ministeres")).andExpect(status().isOk());
    }

    @Test
    void createMinistere_withoutAuth_shouldReturn401() throws Exception {
        Map<String, String> body = Map.of("code", "MIN1", "nom", "Ministère Test");
        mockMvc.perform(post("/api/v1/ministeres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createMinistere_withRoleUser_shouldReturn403() throws Exception {
        Map<String, String> body = Map.of("code", "MIN1", "nom", "Ministère Test");
        mockMvc.perform(post("/api/v1/ministeres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMinistere_withRoleAdmin_shouldReturn201() throws Exception {
        Map<String, String> body = Map.of("code", "MIN1", "nom", "Ministère Test");
        Ministere m = new Ministere();
        when(ministereService.createMinistere(any())).thenReturn(m);
        mockMvc.perform(post("/api/v1/ministeres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isCreated());
    }

    @Test
    void updateMinistere_withRoleAdmin_shouldReturn200() throws Exception {
        Map<String, String> body = Map.of("nom", "Ministère Modifié");
        Ministere m = new Ministere();
        when(ministereService.updateMinistere(eq(1L), any())).thenReturn(m);
        mockMvc.perform(patch("/api/v1/ministeres/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
    }
}
