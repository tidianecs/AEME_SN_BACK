package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Controllers.ChatRESTController;
import com.ditix.backend.Chat.DTO.ChatUserSearchDTO;
import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationMemberRepository;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Chat.Services.ChatService;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ChatUserSearchAndOrderIntegrationTest {

    @Autowired
    private ChatRESTController chatRESTController;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    @Autowired
    private ProfilUtilisateurRepository profilRepository;

    @Autowired
    private MinistereRepository ministereRepository;

    @Autowired
    private StructureRepository structureRepository;

    private ProfilUtilisateur activeAdmin;
    private ProfilUtilisateur activeGestionnaire;
    private ProfilUtilisateur inactiveUser;
    private ProfilUtilisateur searchTarget;

    private JwtAuthenticationToken createMockAuthToken(ProfilUtilisateur profil) {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", profil.getKeycloakId().toString())
                .claim("email", profil.getEmail())
                .claim("realm_access", Map.of("roles", List.of(profil.getRole().name())))
                .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + profil.getRole().name())));
    }

    @BeforeEach
    void setUp() {
        Ministere m = new Ministere();
        m.setNom("Ministere Test " + UUID.randomUUID());
        m.setNomCourt("MT " + UUID.randomUUID().toString().substring(0, 5));
        m.setCode("CODE_M_" + UUID.randomUUID());
        m.setActif(true);
        m = ministereRepository.save(m);

        Structure s = new Structure();
        s.setName("Structure Test " + UUID.randomUUID());
        s.setCode("CODE_S_" + UUID.randomUUID());
        s.setCategorie("CAT");
        s.setActif(true);
        s.setMinistereV2(m);
        s = structureRepository.save(s);

        com.ditix.backend.Cohorte.Model.Cohorte c = new com.ditix.backend.Cohorte.Model.Cohorte();
        c.setNom("Cohorte Test " + UUID.randomUUID());
        c.setCode("COH_" + UUID.randomUUID());
        c.setActif(true);
        c = org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors(
            org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(
                ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest().getServletContext()),
            com.ditix.backend.Cohorte.Repository.CohorteRepository.class
        ).save(c);

        activeAdmin = createProfil("Admin", "Super", "admin@test.com", RoleUtilisateur.ADMIN, true, null, null, null);
        activeGestionnaire = createProfil("Gestionnaire", "Active", "gest@test.com", RoleUtilisateur.GESTIONNAIRE, true, null, s, c);
        inactiveUser = createProfil("Inactive", "User", "inactive@test.com", RoleUtilisateur.GESTIONNAIRE, false, null, s, c);
        searchTarget = createProfil("John", "Doe", "john@test.com", RoleUtilisateur.DAGE, true, m, null, null);
    }

    @AfterEach
    void tearDown() {
        conversationMemberRepository.deleteAll();
        conversationRepository.deleteAll();
        profilRepository.deleteAll();
        structureRepository.deleteAll();
        ministereRepository.deleteAll();
    }

    private ProfilUtilisateur createProfil(String prenom, String nom, String email, RoleUtilisateur role, boolean active, Ministere m, Structure s, com.ditix.backend.Cohorte.Model.Cohorte c) {
        ProfilUtilisateur p = new ProfilUtilisateur();
        p.setPrenom(prenom);
        p.setNom(nom);
        p.setEmail(email);
        p.setKeycloakId(UUID.randomUUID());
        p.setRole(role);
        p.setActif(active);
        p.setMinistere(m);
        p.setStructure(s);
        p.setCohorte(c);
        return profilRepository.save(p);
    }

    @Test
    void testUserSearch_ReturnsActiveUsersExcludingSelf() {
        JwtAuthenticationToken auth = createMockAuthToken(activeGestionnaire);
        
        // Search by first name
        ResponseEntity<List<ChatUserSearchDTO>> response = chatRESTController.searchUsers("john", auth);
        List<ChatUserSearchDTO> users = response.getBody();
        
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("John", users.get(0).getPrenom());
        assertEquals(RoleUtilisateur.DAGE.name(), users.get(0).getRole());
        assertTrue(users.get(0).getMinistereName().startsWith("Ministere Test"));
        
        // Search by last name
        response = chatRESTController.searchUsers("doe", auth);
        assertEquals(1, response.getBody().size());

        // Inactive user should not appear
        response = chatRESTController.searchUsers("inactive", auth);
        assertEquals(0, response.getBody().size());

        // Self should not appear
        response = chatRESTController.searchUsers("Active", auth);
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testUserSearch_EmptyQueryReturnsEmptyList() {
        JwtAuthenticationToken auth = createMockAuthToken(activeAdmin);
        ResponseEntity<List<ChatUserSearchDTO>> response = chatRESTController.searchUsers("   ", auth);
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testConversationOrder_UpdatedOnNewMessage() throws InterruptedException {
        // Create older conversation
        Conversation convA = chatService.getOrCreateConversation(activeAdmin.getKeycloakId().toString(), searchTarget.getKeycloakId().toString());
        // Artificial delay for sorting check
        Thread.sleep(1000);
        // Create newer conversation
        Conversation convB = chatService.getOrCreateConversation(activeAdmin.getKeycloakId().toString(), activeGestionnaire.getKeycloakId().toString());

        JwtAuthenticationToken auth = createMockAuthToken(activeAdmin);
        
        // At this point B is newer than A
        List<Conversation> convs = chatRESTController.getMyConversations(auth).getBody();
        assertEquals(2, convs.size());
        assertEquals(convB.getId(), convs.get(0).getId());
        assertEquals(convA.getId(), convs.get(1).getId());

        // Now admin sends a message in convA
        chatService.saveMessage(convA.getId(), activeAdmin.getKeycloakId().toString(), "Admin", "Hello A");

        // Now A should be newer than B
        convs = chatRESTController.getMyConversations(auth).getBody();
        assertEquals(2, convs.size());
        assertEquals(convA.getId(), convs.get(0).getId(), "Conversation A should have bumped to the top");
        assertEquals(convB.getId(), convs.get(1).getId());
        
        assertNotNull(convs.get(0).getUpdatedAt());
    }
}
