package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationMember;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationMemberRepository;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Chat.Services.ChatGroupMembershipSyncService;
import com.ditix.backend.Chat.Services.ManagedChatGroupService;
import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Repository.CohorteRepository;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AdminChatGroupIntegrationTest {

    @Autowired
    private ProfilUtilisateurRepository profilRepository;

    @Autowired
    private MinistereRepository ministereRepository;

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    @Autowired
    private ChatGroupMembershipSyncService syncService;

    @Autowired
    private ManagedChatGroupService managedChatGroupService;

    private ProfilUtilisateur activeAdminA;
    private ProfilUtilisateur activeAdminB;
    private ProfilUtilisateur inactiveAdmin;
    private ProfilUtilisateur dage;
    private ProfilUtilisateur gestionnaireA;
    private ProfilUtilisateur gestionnaireB;
    private Ministere ministere;
    private Structure structure;
    private Cohorte cohorte;

    @BeforeEach
    void setUp() {
        activeAdminA = createProfil("Admin A", RoleUtilisateur.ADMIN, true, null, null, null);
        activeAdminB = createProfil("Admin B", RoleUtilisateur.ADMIN, true, null, null, null);
        inactiveAdmin = createProfil("Admin Inactive", RoleUtilisateur.ADMIN, false, null, null, null);

        ministere = new Ministere();
        ministere.setNom("Ministry " + UUID.randomUUID());
        ministere.setCode("MIN-" + UUID.randomUUID());
        ministere.setActif(true);
        ministere = ministereRepository.save(ministere);

        dage = createProfil("Dage", RoleUtilisateur.DAGE, true, ministere, null, null);

        structure = new Structure();
        structure.setName("Structure " + UUID.randomUUID());
        structure.setMinistereV2(ministere);
        structure.setActif(true);
        structure = structureRepository.save(structure);

        cohorte = new Cohorte();
        cohorte.setNom("Cohorte " + UUID.randomUUID());
        cohorte.setCode("COH-" + UUID.randomUUID());
        cohorte.setActif(true);
        cohorte = cohorteRepository.save(cohorte);

        gestionnaireA = createProfil("Gestionnaire A", RoleUtilisateur.GESTIONNAIRE, true, null, structure, cohorte);
        gestionnaireB = createProfil("Gestionnaire B", RoleUtilisateur.GESTIONNAIRE, true, null, structure, cohorte);
    }

    @AfterEach
    void tearDown() {
        conversationMemberRepository.deleteAll();
        conversationRepository.deleteAll();
        profilRepository.deleteAll();
        structureRepository.deleteAll();
        cohorteRepository.deleteAll();
        ministereRepository.deleteAll();
    }

    private ProfilUtilisateur createProfil(String name, RoleUtilisateur role, boolean active, Ministere m, Structure s, Cohorte c) {
        ProfilUtilisateur p = new ProfilUtilisateur();
        p.setPrenom(name);
        p.setNom("Test");
        p.setEmail(UUID.randomUUID() + "@example.com");
        p.setKeycloakId(UUID.randomUUID());
        p.setRole(role);
        p.setActif(active);
        p.setMinistere(m);
        p.setStructure(s);
        p.setCohorte(c);
        return profilRepository.save(p);
    }

    @Test
    void testGlobalGroupAdmins() {
        Conversation globalGroup = managedChatGroupService.createOrGetGlobalGroup("Global Group", dage.getKeycloakId().toString());
        syncService.syncGroup(globalGroup.getId());

        List<ConversationMember> members = conversationMemberRepository.findByConversationIdAndActiveTrue(globalGroup.getId());
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(activeAdminA.getKeycloakId().toString())));
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(activeAdminB.getKeycloakId().toString())));
        assertFalse(members.stream().anyMatch(m -> m.getUserId().equals(inactiveAdmin.getKeycloakId().toString())));

        // Also has normal members
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(dage.getKeycloakId().toString())));
    }

    @Test
    void testMinistereGroupAdmins() {
        Conversation ministereGroup = managedChatGroupService.createOrGetMinistereGroup(ministere.getId().toString(), "Ministere Group", activeAdminA.getKeycloakId().toString());
        syncService.syncGroup(ministereGroup.getId());

        List<ConversationMember> members = conversationMemberRepository.findByConversationIdAndActiveTrue(ministereGroup.getId());
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(activeAdminA.getKeycloakId().toString())));
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(activeAdminB.getKeycloakId().toString())));
        assertFalse(members.stream().anyMatch(m -> m.getUserId().equals(inactiveAdmin.getKeycloakId().toString())));
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(dage.getKeycloakId().toString())));
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(gestionnaireA.getKeycloakId().toString())));
    }

    @Test
    void testStructureGroupAdmins() {
        Conversation structureGroup = managedChatGroupService.createOrGetStructureGroup(structure.getId().toString(), "Structure Group", activeAdminA.getKeycloakId().toString());
        syncService.syncGroup(structureGroup.getId());

        List<ConversationMember> members = conversationMemberRepository.findByConversationIdAndActiveTrue(structureGroup.getId());
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(activeAdminA.getKeycloakId().toString())));
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(activeAdminB.getKeycloakId().toString())));
        assertFalse(members.stream().anyMatch(m -> m.getUserId().equals(inactiveAdmin.getKeycloakId().toString())));
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(gestionnaireA.getKeycloakId().toString())));
    }

    @Test
    void testCohorteGroupAdmins() {
        Conversation cohorteGroup = managedChatGroupService.createOrGetCohortGroup(cohorte.getId().toString(), "Cohorte Group", activeAdminA.getKeycloakId().toString());
        syncService.syncGroup(cohorteGroup.getId());

        List<ConversationMember> members = conversationMemberRepository.findByConversationIdAndActiveTrue(cohorteGroup.getId());
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(activeAdminA.getKeycloakId().toString())));
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(activeAdminB.getKeycloakId().toString())));
        assertFalse(members.stream().anyMatch(m -> m.getUserId().equals(inactiveAdmin.getKeycloakId().toString())));
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(gestionnaireA.getKeycloakId().toString())));
    }

    @Test
    void testDirectGroupUnchanged() {
        Conversation direct = new Conversation();
        direct.setType(ConversationType.DIRECT);
        direct.setName("Direct");
        direct.setActive(true);
        direct.setSystemManaged(false);
        direct.setUserOneId(dage.getKeycloakId().toString());
        direct.setUserTwoId(gestionnaireA.getKeycloakId().toString());
        direct = conversationRepository.save(direct);

        ConversationMember m1 = new ConversationMember();
        m1.setConversationId(direct.getId());
        m1.setUserId(dage.getKeycloakId().toString());
        m1.setActive(true);
        conversationMemberRepository.save(m1);

        ConversationMember m2 = new ConversationMember();
        m2.setConversationId(direct.getId());
        m2.setUserId(gestionnaireA.getKeycloakId().toString());
        m2.setActive(true);
        conversationMemberRepository.save(m2);

        // Attempt sync
        final Long directId = direct.getId();
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            syncService.syncGroup(directId); // Should throw bad request
        });

        // Ensure admins were NOT added
        List<ConversationMember> members = conversationMemberRepository.findByConversationIdAndActiveTrue(directId);
        assertEquals(2, members.size());
        assertFalse(members.stream().anyMatch(m -> m.getUserId().equals(activeAdminA.getKeycloakId().toString())));
    }

    @Test
    void testCreationAutoSync() {
        Conversation cohortGroup = managedChatGroupService.createOrGetCohortGroup(cohorte.getId().toString(), "Auto Sync Group", gestionnaireA.getKeycloakId().toString());
        // Do not call syncService.syncGroup manually here.

        List<ConversationMember> members = conversationMemberRepository.findByConversationIdAndActiveTrue(cohortGroup.getId());
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(activeAdminA.getKeycloakId().toString())));
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(activeAdminB.getKeycloakId().toString())));
        assertTrue(members.stream().anyMatch(m -> m.getUserId().equals(gestionnaireA.getKeycloakId().toString())));
    }

    @Test
    void testExistingGroupRepair() {
        // Manually create group without sync
        Long id = conversationRepository.insertGlobalGroupAtomically(ConversationType.GLOBAL.name(), "Orphan Group", null, activeAdminA.getKeycloakId().toString());
        Conversation orphan = conversationRepository.findById(id).orElseThrow();

        // Members empty right now
        List<ConversationMember> beforeMembers = conversationMemberRepository.findByConversationIdAndActiveTrue(id);
        assertEquals(0, beforeMembers.size());

        // Run global sync repair
        syncService.syncAllActiveManagedGroups();

        List<ConversationMember> afterMembers = conversationMemberRepository.findByConversationIdAndActiveTrue(id);
        assertTrue(afterMembers.size() > 0);
        assertTrue(afterMembers.stream().anyMatch(m -> m.getUserId().equals(activeAdminA.getKeycloakId().toString())));
        assertTrue(afterMembers.stream().anyMatch(m -> m.getUserId().equals(activeAdminB.getKeycloakId().toString())));
        assertFalse(afterMembers.stream().anyMatch(m -> m.getUserId().equals(inactiveAdmin.getKeycloakId().toString())));
    }
}
