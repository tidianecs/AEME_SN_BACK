package com.ditix.backend.Chat;

import com.ditix.backend.Chat.DTO.ChatGroupSyncReport;
import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationMember;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationMemberRepository;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Chat.Services.ChatGroupMembershipSyncService;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    private ProfilUtilisateurRepository profilRepository;

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

    @Test
    void testSyncGlobalGroup() {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.GLOBAL);
        conv.setSystemManaged(true);
        conv.setActive(true);
        conv.setName("Global Test");
        conv.setReferenceId("GLOBAL");
        conv = conversationRepository.saveAndFlush(conv);

        when(profilRepository.findActiveGlobalChatMembers()).thenReturn(List.of(
            UUID.randomUUID(), UUID.randomUUID()
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
        conv.setName("Cohort Test");
        conv.setReferenceId("10");
        conv = conversationRepository.saveAndFlush(conv);

        when(profilRepository.findActiveMembersByCohorte(10L)).thenReturn(List.of(
            UUID.randomUUID(), UUID.randomUUID()
        ));

        ChatGroupSyncReport report = syncService.syncGroup(conv.getId());

        assertEquals(2, report.getAddedMembers());
        assertEquals(2, report.getEligibleUsers());
        assertEquals(2, report.getActiveMembersAfter());

        List<ConversationMember> members = conversationMemberRepository.findByConversationIdAndActiveTrue(conv.getId());
        assertEquals(2, members.size());
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

        when(profilRepository.findActiveGlobalChatMembers()).thenReturn(List.of(
            UUID.randomUUID()
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
        assertEquals(1, members.size());
    }

    @Test
    void testDirectoryEmptyList_DeactivatesAll() {
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

        when(profilRepository.findActiveGlobalChatMembers()).thenReturn(java.util.Collections.emptyList());

        ChatGroupSyncReport report = syncService.syncGroup(savedConv.getId());

        assertEquals(0, report.getEligibleUsers());
        assertEquals(1, report.getDeactivatedMembers());
        assertEquals(0, report.getActiveMembersAfter());

        List<ConversationMember> active = conversationMemberRepository.findByConversationIdAndActiveTrue(savedConv.getId());
        assertTrue(active.isEmpty());
    }

    @Test
    void testDirectoryError_ThrowsException() {
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

        when(profilRepository.findActiveGlobalChatMembers()).thenThrow(new RuntimeException("DB down"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> syncService.syncGroup(savedConv.getId()));
        assertEquals("DB down", exception.getMessage());

        List<ConversationMember> active = conversationMemberRepository.findByConversationIdAndActiveTrue(savedConv.getId());
        assertEquals(1, active.size());
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
        conv2.setReferenceId("2");
        final Conversation savedConv2 = conversationRepository.saveAndFlush(conv2);

        when(profilRepository.findActiveGlobalChatMembers()).thenReturn(List.of(UUID.randomUUID()));
        when(profilRepository.findActiveMembersByCohorte(org.mockito.ArgumentMatchers.anyLong())).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Simulated failure"));

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> syncService.syncAllActiveManagedGroups());

        List<ConversationMember> members = conversationMemberRepository.findByConversationIdAndActiveTrue(savedConv1.getId());
        assertTrue(members.isEmpty(), "Conv1 should have no members due to rollback");
    }
}
