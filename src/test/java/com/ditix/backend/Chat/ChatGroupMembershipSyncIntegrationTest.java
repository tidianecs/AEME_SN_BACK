package com.ditix.backend.Chat;

import com.ditix.backend.Chat.DTO.ChatGroupSyncReport;
import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationMember;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationMemberRepository;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Chat.Services.ChatGroupMembershipSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ActiveProfiles("test")
public class ChatGroupMembershipSyncIntegrationTest {

    @Autowired
    private ChatGroupMembershipSyncService syncService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    @MockBean
    private Keycloak keycloak;

    @BeforeEach
    void setUp() {
        conversationMemberRepository.deleteAll();
        conversationRepository.deleteAll();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        conversationMemberRepository.deleteAll();
        conversationRepository.deleteAll();
    }

    private void mockKeycloakUsers(List<UserRepresentation> users) {
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        when(usersResource.list(0, 100)).thenReturn(users);
        when(usersResource.list(100, 100)).thenReturn(Collections.emptyList());
    }

    private UserRepresentation createUser(String id, boolean enabled, String cohorte, String structureId) {
        UserRepresentation u = new UserRepresentation();
        u.setId(id);
        u.setEnabled(enabled);
        Map<String, List<String>> attrs = new HashMap<>();
        if (cohorte != null) attrs.put("cohorte", List.of(cohorte));
        if (structureId != null) attrs.put("structureId", List.of(structureId));
        u.setAttributes(attrs);
        return u;
    }

    @Test
    void testSyncGlobalGroup() {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.GLOBAL);
        conv.setSystemManaged(true);
        conv.setActive(true);
        conv.setName("Global Test");
        conv.setReferenceId("GLOBAL");
        conv = conversationRepository.saveAndFlush(conv);

        mockKeycloakUsers(List.of(
                createUser("u1", true, null, null),
                createUser("u2", true, null, null),
                createUser("u3", false, null, null)
        ));

        ChatGroupSyncReport report = syncService.syncGroup(conv.getId());

        assertEquals(2, report.getAddedMembers());
        assertEquals(2, report.getEligibleUsers());
        assertEquals(2, report.getActiveMembersAfter());

        List<ConversationMember> members = conversationMemberRepository.findByConversationIdAndActiveTrue(conv.getId());
        assertEquals(2, members.size());
    }

    @Test
    void testSyncCohortGroup() {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.COHORT);
        conv.setSystemManaged(true);
        conv.setActive(true);
        conv.setName("Cohort A");
        conv.setReferenceId("COHORT-A");
        conv = conversationRepository.saveAndFlush(conv);

        mockKeycloakUsers(List.of(
                createUser("u1", true, "COHORT-A", null),
                createUser("u2", true, "COHORT-B", null),
                createUser("u3", true, " COHORT-A ", null)
        ));

        ChatGroupSyncReport report = syncService.syncGroup(conv.getId());

        assertEquals(2, report.getAddedMembers());
        assertEquals(2, report.getEligibleUsers());
    }

    @Test
    void testKeycloakEmptyList_DeactivatesAll() {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.GLOBAL);
        conv.setSystemManaged(true);
        conv.setActive(true);
        conv.setName("Global Test");
        conv.setReferenceId("GLOBAL");
        Conversation savedConv = conversationRepository.saveAndFlush(conv);

        ConversationMember member = new ConversationMember();
        member.setConversationId(savedConv.getId());
        member.setUserId("old_user");
        member.setActive(true);
        member.setJoinedAt(java.time.LocalDateTime.now());
        conversationMemberRepository.saveAndFlush(member);

        mockKeycloakUsers(Collections.emptyList());

        ChatGroupSyncReport report = syncService.syncGroup(savedConv.getId());

        assertEquals(0, report.getEligibleUsers());
        assertEquals(1, report.getDeactivatedMembers());
        assertEquals(0, report.getActiveMembersAfter());

        List<ConversationMember> active = conversationMemberRepository.findByConversationIdAndActiveTrue(savedConv.getId());
        assertTrue(active.isEmpty());
    }

    @Test
    void testKeycloakError_NoChangesMade() {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.GLOBAL);
        conv.setSystemManaged(true);
        conv.setActive(true);
        conv.setName("Global Test");
        conv.setReferenceId("GLOBAL");
        Conversation savedConv = conversationRepository.saveAndFlush(conv);

        ConversationMember member = new ConversationMember();
        member.setConversationId(savedConv.getId());
        member.setUserId("old_user");
        member.setActive(true);
        member.setJoinedAt(java.time.LocalDateTime.now());
        conversationMemberRepository.saveAndFlush(member);

        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list(anyInt(), anyInt())).thenThrow(new RuntimeException("Keycloak down"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> syncService.syncGroup(savedConv.getId()));
        assertEquals(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        assertEquals("User directory is temporarily unavailable", exception.getReason());
        assertFalse(exception.getMessage().contains("Keycloak down"));

        List<ConversationMember> active = conversationMemberRepository.findByConversationIdAndActiveTrue(savedConv.getId());
        assertEquals(1, active.size());
    }

    @Test
    void testSyncConcurrent() throws InterruptedException {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.GLOBAL);
        conv.setSystemManaged(true);
        conv.setActive(true);
        conv.setName("Global Test");
        conv.setReferenceId("GLOBAL");
        Conversation savedConv = conversationRepository.saveAndFlush(conv);

        mockKeycloakUsers(List.of(
                createUser("u1", true, null, null)
        ));

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    syncService.syncGroup(savedConv.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    done.countDown();
                }
            });
        }

        latch.countDown();
        done.await();

        assertEquals(2, successCount.get());
        List<ConversationMember> members = conversationMemberRepository.findByConversationIdAndActiveTrue(savedConv.getId());
        assertEquals(1, members.size()); // Should only be 1 member
    }

    @Test
    void testSyncAllGroups_RollbackOnFailure() {
        Conversation conv1 = new Conversation();
        conv1.setType(ConversationType.GLOBAL);
        conv1.setSystemManaged(true);
        conv1.setActive(true);
        conv1.setName("Global Test 1");
        conv1.setReferenceId("GLOBAL1");
        final Conversation savedConv1 = conversationRepository.saveAndFlush(conv1);

        Conversation conv2 = new Conversation();
        conv2.setType(ConversationType.COHORT);
        conv2.setSystemManaged(true);
        conv2.setActive(true);
        conv2.setName("Cohort Test 2");
        conv2.setReferenceId("COHORT2");
        final Conversation savedConv2 = conversationRepository.saveAndFlush(conv2);

        mockKeycloakUsers(List.of(
                createUser("u1", true, null, null)
        ));

        // Mock an exception on insert for conv2 to simulate failure mid-transaction
        // Since we cannot easily mock the repository within @SpringBootTest for just one call,
        // we will intentionally pass a too-long string to cause a DataIntegrityViolationException on user_id

        mockKeycloakUsers(List.of(
                createUser("u1", true, null, null),
                createUser("u".repeat(300), true, "COHORT2", null) // This will fail because user_id max length is 255
        ));

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> syncService.syncAllActiveManagedGroups());

        // Verify that conv1 was NOT modified (rollback happened)
        List<ConversationMember> members = conversationMemberRepository.findByConversationIdAndActiveTrue(savedConv1.getId());
        assertTrue(members.isEmpty(), "Conv1 should have no members due to rollback");
    }
}
