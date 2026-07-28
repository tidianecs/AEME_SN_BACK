package com.ditix.backend.Structure;

import com.ditix.backend.Core.SecurityConfig;
import com.ditix.backend.Structure.Controllers.StructureController;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Services.StructureService;
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

@WebMvcTest(StructureController.class)
@Import(SecurityConfig.class)
public class StructureSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StructureService structureService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllStructures_withoutAuth_shouldReturn200() throws Exception {
        when(structureService.getAllStructures()).thenReturn(List.of());
        
        mockMvc.perform(get("/api/v1/structures"))
                .andExpect(status().isOk());
                
        verify(structureService).getAllStructures();
    }

    @Test
    void getStructureById_withoutAuth_shouldReturn200() throws Exception {
        Structure s = new Structure();
        when(structureService.getStructureById(1L)).thenReturn(s);
        
        mockMvc.perform(get("/api/v1/structures/1"))
                .andExpect(status().isOk());
                
        verify(structureService).getStructureById(1L);
    }

    @Test
    void createStructure_withoutAuth_shouldReturn401() throws Exception {
        Map<String, String> body = Map.of("name", "Test Structure");
        
        mockMvc.perform(post("/api/v1/structures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
                
        verify(structureService, never()).createStructure(any());
    }

    @Test
    void createStructure_withRoleUser_shouldReturn403() throws Exception {
        Map<String, String> body = Map.of("name", "Test Structure");
        
        mockMvc.perform(post("/api/v1/structures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
                
        verify(structureService, never()).createStructure(any());
    }

    @Test
    void createStructure_withRoleAdmin_shouldReturn201() throws Exception {
        Map<String, String> body = Map.of("name", "Test Structure");
        Structure s = new Structure();
        when(structureService.createStructure(any())).thenReturn(s);
        
        mockMvc.perform(post("/api/v1/structures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isCreated());
                
        verify(structureService).createStructure(any());
    }

    @Test
    void updateStructure_withoutAuth_shouldReturn401() throws Exception {
        Map<String, String> body = Map.of("name", "Test Structure");
        
        mockMvc.perform(patch("/api/v1/structures/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
                
        verify(structureService, never()).updateStructure(any(), any());
    }

    @Test
    void updateStructure_withRoleUser_shouldReturn403() throws Exception {
        Map<String, String> body = Map.of("name", "Test Structure");
        
        mockMvc.perform(patch("/api/v1/structures/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
                
        verify(structureService, never()).updateStructure(any(), any());
    }

    @Test
    void updateStructure_withRoleAdmin_shouldReturn200() throws Exception {
        Map<String, String> body = Map.of("name", "Test Structure");
        Structure s = new Structure();
        when(structureService.updateStructure(eq(1L), any())).thenReturn(s);
        
        mockMvc.perform(patch("/api/v1/structures/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
                
        verify(structureService).updateStructure(eq(1L), any());
    }

    @Test
    void deleteStructure_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/v1/structures/1"))
                .andExpect(status().isUnauthorized());
                
        verify(structureService, never()).deleteStructure(any());
    }

    @Test
    void deleteStructure_withRoleUser_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/v1/structures/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
                
        verify(structureService, never()).deleteStructure(any());
    }

    @Test
    void deleteStructure_withRoleAdmin_shouldReturn200() throws Exception {
        doNothing().when(structureService).deleteStructure(1L);
        
        mockMvc.perform(delete("/api/v1/structures/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
                
        verify(structureService).deleteStructure(1L);
    }
}
