package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Repository.CohorteRepository;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProfilUtilisateurGetMeLazyLoadingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MinistereRepository ministereRepository;

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    @Test
    public void testGetMeDage() throws Exception {
        // 1. Create a Ministere
        Ministere ministere = new Ministere();
        ministere.setNom("Ministère V2 " + UUID.randomUUID().toString());
        ministere.setCode("MIN-V2-" + UUID.randomUUID().toString());
        ministere.setActif(true);
        ministere = ministereRepository.save(ministere);

        // 2. Create DAGE user with Ministere linked directly
        ProfilUtilisateur dage = new ProfilUtilisateur();
        dage.setPrenom("Dage");
        dage.setNom("Test");
        dage.setEmail("dage.test." + UUID.randomUUID().toString() + "@example.com");
        dage.setKeycloakId(UUID.randomUUID());
        dage.setRole(RoleUtilisateur.DAGE);
        dage.setActif(true);
        dage.setMinistere(ministere);
        profilUtilisateurRepository.save(dage);

        // Perform GET /api/v2/me and expect 200 OK because DAGE has a ministere linked
        mockMvc.perform(get("/api/v2/me")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(builder -> builder.claim("email", dage.getEmail()).subject(dage.getKeycloakId().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_dage"))))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetMeGestionnaireSuccess() throws Exception {
        // 1. Create a Ministere
        Ministere ministere = new Ministere();
        ministere.setNom("Ministère V2 " + UUID.randomUUID().toString());
        ministere.setCode("MIN-V2-" + UUID.randomUUID().toString());
        ministere.setActif(true);
        ministere = ministereRepository.save(ministere);

        // 2. Create a Structure linked to the Ministere
        Structure structure = new Structure();
        structure.setName("Structure GESTIONNAIRE " + UUID.randomUUID().toString());
        structure.setMinistereV2(ministere);
        structure.setActif(true);
        structure = structureRepository.save(structure);

        // 3. Create a Cohorte
        Cohorte cohorte = new Cohorte();
        cohorte.setNom("Cohorte GESTIONNAIRE " + UUID.randomUUID().toString());
        cohorte.setCode("COH-GES-" + UUID.randomUUID().toString());
        cohorte.setActif(true);
        cohorte = cohorteRepository.save(cohorte);

        // 4. Create GESTIONNAIRE user without direct Ministere, but with Structure and Cohorte
        ProfilUtilisateur gestionnaire = new ProfilUtilisateur();
        gestionnaire.setPrenom("Gestionnaire");
        gestionnaire.setNom("Test");
        gestionnaire.setEmail("gestionnaire.test." + UUID.randomUUID().toString() + "@example.com");
        gestionnaire.setKeycloakId(UUID.randomUUID());
        gestionnaire.setRole(RoleUtilisateur.GESTIONNAIRE);
        gestionnaire.setActif(true);
        gestionnaire.setStructure(structure);
        gestionnaire.setCohorte(cohorte);
        profilUtilisateurRepository.save(gestionnaire);

        // Perform GET /api/v2/me and expect 200 OK, verifying derived ministere
        mockMvc.perform(get("/api/v2/me")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(builder -> builder.claim("email", gestionnaire.getEmail()).subject(gestionnaire.getKeycloakId().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_gestionnaire"))))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.role").value("GESTIONNAIRE"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.structure.id").value(structure.getId()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.cohorte.id").value(cohorte.getId()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.ministere.id").value(ministere.getId()));
    }
}
