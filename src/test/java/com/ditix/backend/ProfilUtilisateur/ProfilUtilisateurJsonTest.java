package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.ProfilUtilisateur.Controllers.ProfilUtilisateurController;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.AutorisationMetierService;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurLectureService;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Cohorte.Model.Cohorte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ProfilUtilisateurJsonTest {

    private MockMvc mockMvc;

    @Mock
    private ProfilUtilisateurCourantService profilUtilisateurCourantService;

    @Mock
    private AutorisationMetierService autorisationMetierService;

    @Mock
    private ProfilUtilisateurLectureService profilUtilisateurLectureService;

    @InjectMocks
    private ProfilUtilisateurController controller;

    private ProfilUtilisateur profil;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        profil = new ProfilUtilisateur();
        profil.setId(10L);
        profil.setKeycloakId(UUID.randomUUID());
        profil.setPrenom("Test");
        profil.setNom("User");
        profil.setEmail("test@test.com");
        profil.setRole(RoleUtilisateur.GESTIONNAIRE);

        Structure s = new Structure();
        s.setId(20L);
        s.setName("Structure A");

        Ministere m = new Ministere();
        m.setId(30L);
        m.setNom("Ministere A");
        s.setMinistereV2(m);

        Cohorte c = new Cohorte();
        c.setId(40L);
        c.setNom("Cohorte 1");

        profil.setStructure(s);
        profil.setCohorte(c);
    }

    @Test
    void testGetMe_JsonSerialization() throws Exception {
        when(profilUtilisateurCourantService.obtenirProfilCourant(any())).thenReturn(profil);

        JwtAuthenticationToken mockToken = org.mockito.Mockito.mock(org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken.class);

        mockMvc.perform(get("/api/v2/me")
                .principal(mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.prenom").value("Test"))
                .andExpect(jsonPath("$.role").value("GESTIONNAIRE"))
                // Sous-DTOs vérifiés
                .andExpect(jsonPath("$.ministere.id").value(30))
                .andExpect(jsonPath("$.structure.id").value(20))
                .andExpect(jsonPath("$.cohorte.id").value(40))
                // Sécurité: absence stricte de keycloakId
                .andExpect(jsonPath("$.keycloakId").doesNotExist())
                .andExpect(jsonPath("$.keycloak_id").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.credentials").doesNotExist())
                .andExpect(jsonPath("$.requiredActions").doesNotExist())
                // Anciennes chaînes V1
                .andExpect(jsonPath("$.membershipService").doesNotExist())
                .andExpect(jsonPath("$.structureId").doesNotExist())
                .andExpect(jsonPath("$.serviceLatitude").doesNotExist())
                .andExpect(jsonPath("$.serviceLongitude").doesNotExist());
    }
}
