package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.Auth.Services.AuthService;
import com.ditix.backend.ProfilUtilisateur.DTO.ModifierMonProfilRequest;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProfilUtilisateurMeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfilUtilisateurRepository profilRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private UUID userKeycloakId;
    private ProfilUtilisateur savedProfil;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM reports");
        jdbcTemplate.execute("DELETE FROM meeting_participants");
        jdbcTemplate.execute("DELETE FROM meetings");
        jdbcTemplate.execute("DELETE FROM conversation_members");
        jdbcTemplate.execute("DELETE FROM conversations");
        profilRepository.deleteAll();

        userKeycloakId = UUID.randomUUID();
        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setKeycloakId(userKeycloakId);
        profil.setPrenom("Ousmane");
        profil.setNom("Soumare");
        profil.setEmail(userKeycloakId.toString() + "@test.com");
        profil.setRole(RoleUtilisateur.ADMIN);
        profil.setPosteOccupe("Gestionnaire");
        profil.setActif(true);

        savedProfil = profilRepository.saveAndFlush(profil);
    }

    @Test
    void testUpdateMe_Success_PartialUpdate() throws Exception {
        ModifierMonProfilRequest request = new ModifierMonProfilRequest();
        request.setPosteOccupe("Responsable énergie");

        mockMvc.perform(patch("/api/v2/me")
                        .with(jwt().jwt(builder -> builder.subject(userKeycloakId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posteOccupe").value("Responsable énergie"))
                .andExpect(jsonPath("$.prenom").value("Ousmane")) // Unchanged
                .andExpect(jsonPath("$.nom").value("Soumare")); // Unchanged

        verify(authService, never()).updateUserProfile(any(), any());
    }

    @Test
    void testUpdateMe_Success_PrenomNom_SyncKeycloak() throws Exception {
        ModifierMonProfilRequest request = new ModifierMonProfilRequest();
        request.setPrenom("NouveauPrenom");
        request.setNom("NouveauNom");

        mockMvc.perform(patch("/api/v2/me")
                        .with(jwt().jwt(builder -> builder.subject(userKeycloakId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prenom").value("NouveauPrenom"))
                .andExpect(jsonPath("$.nom").value("NouveauNom"));

        verify(authService, times(1)).updateUserProfile(eq(userKeycloakId.toString()), argThat(map ->
                map.containsKey("firstName") && map.get("firstName").equals("NouveauPrenom") &&
                map.containsKey("lastName") && map.get("lastName").equals("NouveauNom")
        ));
    }

    @Test
    void testUpdateMe_Forbidden_NoJwt() throws Exception {
        ModifierMonProfilRequest request = new ModifierMonProfilRequest();
        request.setPrenom("Ousmane");

        mockMvc.perform(patch("/api/v2/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateMe_Forbidden_ProfileMissing() throws Exception {
        UUID unknownId = UUID.randomUUID();
        ModifierMonProfilRequest request = new ModifierMonProfilRequest();
        request.setPrenom("Ousmane");

        mockMvc.perform(patch("/api/v2/me")
                        .with(jwt().jwt(builder -> builder.subject(unknownId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateMe_Forbidden_InactiveProfile() throws Exception {
        savedProfil.setActif(false);
        profilRepository.saveAndFlush(savedProfil);

        ModifierMonProfilRequest request = new ModifierMonProfilRequest();
        request.setPrenom("Ousmane");

        mockMvc.perform(patch("/api/v2/me")
                        .with(jwt().jwt(builder -> builder.subject(userKeycloakId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateMe_ForbiddenFields_Ignored() throws Exception {
        String payload = """
        {
            "prenom": "Ousmane2",
            "role": "ADMIN",
            "actif": false,
            "email": "hacked@test.com",
            "keycloakId": "00000000-0000-0000-0000-000000000000"
        }
        """;

        mockMvc.perform(patch("/api/v2/me")
                        .with(jwt().jwt(builder -> builder.subject(userKeycloakId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prenom").value("Ousmane2"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value(savedProfil.getEmail()))
                .andExpect(jsonPath("$.actif").value(true));
    }
}
