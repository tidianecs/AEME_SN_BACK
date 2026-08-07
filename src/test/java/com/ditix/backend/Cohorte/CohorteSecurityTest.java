package com.ditix.backend.Cohorte;

import com.ditix.backend.Cohorte.Controllers.CohorteController;
import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Services.CohorteService;
import com.ditix.backend.Core.SecurityConfig;
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

@WebMvcTest(CohorteController.class)
@Import(SecurityConfig.class)
public class CohorteSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CohorteService cohorteService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCohortes_withoutAuth_shouldReturn200() throws Exception {
        when(cohorteService.getAllCohortes()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/cohortes")).andExpect(status().isOk());
    }

    @Test
    void createCohorte_withoutAuth_shouldReturn401() throws Exception {
        Map<String, String> body = Map.of("code", "COH3", "nom", "Cohorte 3");
        mockMvc.perform(post("/api/v1/cohortes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCohorte_withRoleUser_shouldReturn403() throws Exception {
        Map<String, String> body = Map.of("code", "COH3", "nom", "Cohorte 3");
        mockMvc.perform(post("/api/v1/cohortes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCohorte_withRoleAdmin_shouldReturn201() throws Exception {
        Map<String, String> body = Map.of("code", "COH3", "nom", "Cohorte 3");
        Cohorte c = new Cohorte();
        when(cohorteService.createCohorte(any())).thenReturn(c);
        mockMvc.perform(post("/api/v1/cohortes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isCreated());
    }

    @Test
    void updateCohorte_withRoleAdmin_shouldReturn200() throws Exception {
        Map<String, String> body = Map.of("nom", "Cohorte Modifiée");
        Cohorte c = new Cohorte();
        when(cohorteService.updateCohorte(eq(1L), any())).thenReturn(c);
        mockMvc.perform(patch("/api/v1/cohortes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
    }
}
