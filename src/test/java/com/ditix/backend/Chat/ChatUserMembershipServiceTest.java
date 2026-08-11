package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationMember;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationMemberRepository;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Chat.Services.ChatUserMembershipService;
import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.Structure.Model.Structure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatUserMembershipServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMemberRepository conversationMemberRepository;

    @InjectMocks
    private ChatUserMembershipService service;

    private ProfilUtilisateur adminProfil;
    private ProfilUtilisateur dageProfil;
    private ProfilUtilisateur gestionnaireProfil;
    private String userId = UUID.randomUUID().toString();

    private Conversation globalConv;
    private Conversation ministereConv;
    private Conversation cohortConv;
    private Conversation structureConv;
    private Conversation directConv;

    @BeforeEach
    void setUp() {
        adminProfil = new ProfilUtilisateur();
        adminProfil.setKeycloakId(UUID.fromString(userId));
        adminProfil.setRole(RoleUtilisateur.ADMIN);

        Ministere ministere = new Ministere();
        ministere.setId(10L);

        dageProfil = new ProfilUtilisateur();
        dageProfil.setKeycloakId(UUID.fromString(userId));
        dageProfil.setRole(RoleUtilisateur.DAGE);
        dageProfil.setMinistere(ministere);

        Cohorte cohorte = new Cohorte();
        cohorte.setId(20L);

        Structure structure = new Structure();
        structure.setId(30L);
        structure.setMinistereV2(ministere);

        gestionnaireProfil = new ProfilUtilisateur();
        gestionnaireProfil.setKeycloakId(UUID.fromString(userId));
        gestionnaireProfil.setRole(RoleUtilisateur.GESTIONNAIRE);
        gestionnaireProfil.setCohorte(cohorte);
        gestionnaireProfil.setStructure(structure);

        globalConv = new Conversation();
        globalConv.setId(1L);
        globalConv.setType(ConversationType.GLOBAL);
        globalConv.setSystemManaged(true);

        ministereConv = new Conversation();
        ministereConv.setId(2L);
        ministereConv.setType(ConversationType.MINISTERE);
        ministereConv.setSystemManaged(true);

        cohortConv = new Conversation();
        cohortConv.setId(3L);
        cohortConv.setType(ConversationType.COHORT);
        cohortConv.setSystemManaged(true);

        structureConv = new Conversation();
        structureConv.setId(4L);
        structureConv.setType(ConversationType.STRUCTURE);
        structureConv.setSystemManaged(true);

        directConv = new Conversation();
        directConv.setId(99L);
        directConv.setType(ConversationType.DIRECT);
        directConv.setSystemManaged(false);
    }

    @Test
    void testAdminJoinsGlobalOnly() {
        when(conversationRepository.findByTypeAndActiveTrue(ConversationType.GLOBAL))
                .thenReturn(Optional.of(globalConv));

        service.syncUserMemberships(adminProfil);

        verify(conversationMemberRepository).insertSyncMemberIfAbsent(1L, userId);
        verify(conversationMemberRepository, never()).insertSyncMemberIfAbsent(eq(2L), anyString());
        verify(conversationMemberRepository, never()).insertSyncMemberIfAbsent(eq(3L), anyString());
        verify(conversationMemberRepository, never()).insertSyncMemberIfAbsent(eq(4L), anyString());
    }

    @Test
    void testDageJoinsGlobalAndMinistere() {
        when(conversationRepository.findByTypeAndActiveTrue(ConversationType.GLOBAL))
                .thenReturn(Optional.of(globalConv));
        when(conversationRepository.findByTypeAndReferenceIdAndActiveTrue(ConversationType.MINISTERE, "10"))
                .thenReturn(Optional.of(ministereConv));

        service.syncUserMemberships(dageProfil);

        verify(conversationMemberRepository).insertSyncMemberIfAbsent(1L, userId);
        verify(conversationMemberRepository).insertSyncMemberIfAbsent(2L, userId);
        verify(conversationMemberRepository, never()).insertSyncMemberIfAbsent(eq(3L), anyString());
        verify(conversationMemberRepository, never()).insertSyncMemberIfAbsent(eq(4L), anyString());
    }

    @Test
    void testGestionnaireJoinsExpectedGroups() {
        when(conversationRepository.findByTypeAndActiveTrue(ConversationType.GLOBAL))
                .thenReturn(Optional.of(globalConv));
        when(conversationRepository.findByTypeAndReferenceIdAndActiveTrue(ConversationType.MINISTERE, "10"))
                .thenReturn(Optional.of(ministereConv));
        when(conversationRepository.findByTypeAndReferenceIdAndActiveTrue(ConversationType.COHORT, "20"))
                .thenReturn(Optional.of(cohortConv));
        when(conversationRepository.findByTypeAndReferenceIdAndActiveTrue(ConversationType.STRUCTURE, "30"))
                .thenReturn(Optional.of(structureConv));

        service.syncUserMemberships(gestionnaireProfil);

        verify(conversationMemberRepository).insertSyncMemberIfAbsent(1L, userId);
        verify(conversationMemberRepository).insertSyncMemberIfAbsent(2L, userId);
        verify(conversationMemberRepository).insertSyncMemberIfAbsent(3L, userId);
        verify(conversationMemberRepository).insertSyncMemberIfAbsent(4L, userId);
    }

    @Test
    void testObsoleteMembershipDeactivated() {
        when(conversationRepository.findByTypeAndActiveTrue(ConversationType.GLOBAL))
                .thenReturn(Optional.of(globalConv));

        ConversationMember cm2 = new ConversationMember();
        cm2.setConversationId(2L); // User has an obsolete ministere group (maybe they were a DAGE before)

        when(conversationMemberRepository.findByUserIdAndActiveTrue(userId))
                .thenReturn(List.of(cm2));

        when(conversationRepository.findAllById(List.of(2L)))
                .thenReturn(List.of(ministereConv));

        service.syncUserMemberships(adminProfil);

        verify(conversationMemberRepository).insertSyncMemberIfAbsent(1L, userId);
        verify(conversationMemberRepository).deactivateSpecificMember(2L, userId);
    }

    @Test
    void testDirectUntouched() {
        when(conversationRepository.findByTypeAndActiveTrue(ConversationType.GLOBAL))
                .thenReturn(Optional.of(globalConv));

        ConversationMember cm99 = new ConversationMember();
        cm99.setConversationId(99L); // Direct conversation

        when(conversationMemberRepository.findByUserIdAndActiveTrue(userId))
                .thenReturn(List.of(cm99));

        when(conversationRepository.findAllById(List.of(99L)))
                .thenReturn(List.of(directConv));

        service.syncUserMemberships(adminProfil);

        verify(conversationMemberRepository).insertSyncMemberIfAbsent(1L, userId);
        verify(conversationMemberRepository, never()).deactivateSpecificMember(anyLong(), anyString());
    }

    @Test
    void testMissingGroupPolicy() {
        when(conversationRepository.findByTypeAndActiveTrue(ConversationType.GLOBAL))
                .thenReturn(Optional.of(globalConv));
        // Simulate missing MINISTERE group
        when(conversationRepository.findByTypeAndReferenceIdAndActiveTrue(ConversationType.MINISTERE, "10"))
                .thenReturn(Optional.empty());

        service.syncUserMemberships(dageProfil);

        verify(conversationMemberRepository).insertSyncMemberIfAbsent(1L, userId);
        verify(conversationMemberRepository, never()).insertSyncMemberIfAbsent(eq(2L), anyString());
    }

    @Test
    void testInactiveExpectedMembershipIsReactivated() {
        when(conversationRepository.findByTypeAndActiveTrue(ConversationType.GLOBAL))
                .thenReturn(Optional.of(globalConv));

        ConversationMember inactiveMember = new ConversationMember();
        inactiveMember.setId(5L);
        inactiveMember.setConversationId(1L);
        inactiveMember.setUserId(userId);
        inactiveMember.setActive(false);

        when(conversationMemberRepository.findByUserIdAndActiveTrue(userId))
                .thenReturn(List.of());

        when(conversationMemberRepository.findFirstByConversationIdAndUserIdOrderByIdDesc(1L, userId))
                .thenReturn(Optional.of(inactiveMember));

        service.syncUserMemberships(adminProfil);

        verify(conversationMemberRepository, never()).insertSyncMemberIfAbsent(1L, userId);
        verify(conversationMemberRepository).save(inactiveMember);
        org.junit.jupiter.api.Assertions.assertTrue(inactiveMember.isActive());
        org.junit.jupiter.api.Assertions.assertNull(inactiveMember.getLeftAt());
        org.junit.jupiter.api.Assertions.assertNotNull(inactiveMember.getJoinedAt());
    }
    @Test
    void testNonManagedUntouched() {
        when(conversationRepository.findByTypeAndActiveTrue(ConversationType.GLOBAL))
                .thenReturn(Optional.of(globalConv));

        ConversationMember cm99 = new ConversationMember();
        cm99.setConversationId(99L); // Non-managed conversation

        Conversation nonManagedConv = new Conversation();
        nonManagedConv.setId(99L);
        nonManagedConv.setType(ConversationType.GLOBAL);
        nonManagedConv.setSystemManaged(false);

        when(conversationMemberRepository.findByUserIdAndActiveTrue(userId))
                .thenReturn(List.of(cm99));

        when(conversationRepository.findAllById(List.of(99L)))
                .thenReturn(List.of(nonManagedConv));

        service.syncUserMemberships(adminProfil);

        verify(conversationMemberRepository).insertSyncMemberIfAbsent(1L, userId);
        verify(conversationMemberRepository, never()).deactivateSpecificMember(anyLong(), anyString());
    }
}
