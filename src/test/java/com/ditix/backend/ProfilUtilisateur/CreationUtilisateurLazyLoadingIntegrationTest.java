package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Repository.CohorteRepository;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import com.ditix.backend.ProfilUtilisateur.DTO.CreerUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import com.ditix.backend.ProfilUtilisateur.Services.GestionCompteKeycloakService;
import com.ditix.backend.Chat.Services.ChatUserMembershipService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CreationUtilisateurLazyLoadingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MinistereRepository ministereRepository;

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    @MockBean
    private GestionCompteKeycloakService gestionCompteKeycloakService;

    @MockBean
    private ChatUserMembershipService chatUserMembershipService;

    @Test
    public void testCreationGestionnaireWithLazyMinistereV2() throws Exception {
        // Mock Keycloak and Chat
        when(gestionCompteKeycloakService.creerIdentite(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(UUID.randomUUID());
        doNothing().when(gestionCompteKeycloakService).envoyerActionsInitiales(any(UUID.class));
        doNothing().when(chatUserMembershipService).syncUserMemberships(any(ProfilUtilisateur.class));

        // Create Admin user in DB
        ProfilUtilisateur admin = new ProfilUtilisateur();
        admin.setPrenom("Admin");
        admin.setNom("Super");
        admin.setEmail("admin.test." + UUID.randomUUID().toString() + "@example.com");
        admin.setKeycloakId(UUID.randomUUID());
        admin.setRole(RoleUtilisateur.ADMIN);
        admin.setActif(true);
        profilUtilisateurRepository.save(admin);

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

        // 4. Prepare the request
        String gestionnaireEmail = "gestionnaire.test." + UUID.randomUUID().toString() + "@example.com";
        CreerUtilisateurRequest request = new CreerUtilisateurRequest();
        request.setPrenom("Test");
        request.setNom("Gestionnaire");
        request.setEmail(gestionnaireEmail);
        request.setTelephonePrincipal("770000000");
        request.setRole(RoleUtilisateur.GESTIONNAIRE);
        request.setStructureId(structure.getId());
        request.setCohorteId(cohorte.getId());
        request.setDateNaissance(LocalDate.of(1990, 1, 1));
        request.setGenre("M");

        // 5. Perform the POST request as an ADMIN
        mockMvc.perform(post("/api/v2/admin/utilisateurs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().jwt(builder -> builder.claim("email", admin.getEmail()).subject(admin.getKeycloakId().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isCreated());

        // 6. Verify the persisted user
        ProfilUtilisateur createdUser = profilUtilisateurRepository.findByEmailIgnoreCase(gestionnaireEmail).orElse(null);
        assertNotNull(createdUser);
        assertEquals(RoleUtilisateur.GESTIONNAIRE, createdUser.getRole());
        assertEquals(structure.getId(), createdUser.getStructure().getId());
        assertEquals(cohorte.getId(), createdUser.getCohorte().getId());
    }
}
