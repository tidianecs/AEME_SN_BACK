package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationMemberRepository;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Chat.Services.ChatService;
import com.ditix.backend.Chat.Services.ManagedChatGroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.transaction.annotation.Propagation;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ManagedChatGroupIntegrationTest {

    @Autowired
    private ManagedChatGroupService managedChatGroupService;

    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    @Autowired
    private ChatService chatService;

    @Test
    public void testGlobalGroupCreation_Idempotent() {
        Conversation c1 = managedChatGroupService.createOrGetGlobalGroup("Global Team", "admin1");
        assertNotNull(c1.getId());
        assertEquals(ConversationType.GLOBAL, c1.getType());
        assertEquals("GLOBAL", c1.getReferenceId());
        assertTrue(c1.isSystemManaged());
        assertTrue(c1.isActive());
        assertNull(c1.getUserOneId());
        assertNull(c1.getUserTwoId());

        Conversation c2 = managedChatGroupService.createOrGetGlobalGroup("Global Team Changed", "admin2");
        assertEquals(c1.getId(), c2.getId());

        // No membership added
        assertFalse(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(c1.getId(), "admin1"));
    }

    @Test
    public void testCohortGroup_RequiresReference() {
        assertThrows(ResponseStatusException.class, () -> {
            managedChatGroupService.createOrGetCohortGroup(null, "Cohort A", "admin1");
        });
        assertThrows(ResponseStatusException.class, () -> {
            managedChatGroupService.createOrGetCohortGroup("   ", "Cohort A", "admin1");
        });
    }

    @Test
    public void testCohortGroup_Idempotent() {
        Conversation c1 = managedChatGroupService.createOrGetCohortGroup("ref-123", "Cohort A", "admin1");
        Conversation c2 = managedChatGroupService.createOrGetCohortGroup("ref-123", "Cohort B", "admin2");
        assertEquals(c1.getId(), c2.getId());
        assertEquals("Cohort A", c2.getName());
    }

    @Test
    public void testStructureGroup_CohortAndStructureShareReference() {
        Conversation cohort = managedChatGroupService.createOrGetCohortGroup("ref-456", "Cohort X", "admin1");
        Conversation structure = managedChatGroupService.createOrGetStructureGroup("ref-456", "Structure X", "admin1");

        assertNotEquals(cohort.getId(), structure.getId());
        assertEquals(ConversationType.COHORT, cohort.getType());
        assertEquals(ConversationType.STRUCTURE, structure.getType());
        assertEquals("ref-456", cohort.getReferenceId());
        assertEquals("ref-456", structure.getReferenceId());
    }

    @Test
    public void testArchiveAndReactivate() {
        Conversation c1 = managedChatGroupService.createOrGetCohortGroup("ref-archive", "To Archive", "admin1");
        
        Conversation archived = managedChatGroupService.archiveGroup(c1.getId(), "admin1");
        assertFalse(archived.isActive());
        assertNotNull(archived.getUpdatedAt());

        // Inaccessible via standard chat control
        assertFalse(chatService.canAccessConversation(c1.getId(), "admin1"));

        Conversation reactivated = managedChatGroupService.reactivateGroup(c1.getId(), "admin1");
        assertTrue(reactivated.isActive());
        assertNotNull(reactivated.getUpdatedAt());
    }

    @Test
    public void testArchiveDirect_Refused() {
        Conversation direct = new Conversation();
        direct.setType(ConversationType.DIRECT);
        direct.setActive(true);
        direct.setUserOneId("u1");
        direct.setUserTwoId("u2");
        direct = conversationRepository.saveAndFlush(direct);

        final Long directId = direct.getId();
        assertThrows(ResponseStatusException.class, () -> {
            managedChatGroupService.archiveGroup(directId, "admin1");
        });
        
        assertThrows(ResponseStatusException.class, () -> {
            managedChatGroupService.reactivateGroup(directId, "admin1");
        });
    }

    @Test
    public void testListGroups_ExcludesDirect() {
        managedChatGroupService.createOrGetGlobalGroup("Global", "admin1");
        
        Conversation direct = new Conversation();
        direct.setType(ConversationType.DIRECT);
        direct.setActive(true);
        direct.setUserOneId("u1");
        direct.setUserTwoId("u2");
        conversationRepository.saveAndFlush(direct);

        List<Conversation> groups = managedChatGroupService.listManagedGroups();
        assertFalse(groups.stream().anyMatch(g -> g.getType() == ConversationType.DIRECT));
        assertTrue(groups.stream().anyMatch(g -> g.getType() == ConversationType.GLOBAL));
    }

    @Test
    public void testNameAndReferenceIdValidation() {
        assertThrows(ResponseStatusException.class, () -> managedChatGroupService.createOrGetGlobalGroup("", "admin"));
        assertThrows(ResponseStatusException.class, () -> managedChatGroupService.createOrGetGlobalGroup("   ", "admin"));
        
        String longString = "a".repeat(256);
        assertThrows(ResponseStatusException.class, () -> managedChatGroupService.createOrGetGlobalGroup(longString, "admin"));
        assertThrows(ResponseStatusException.class, () -> managedChatGroupService.createOrGetCohortGroup(java.util.UUID.randomUUID().toString(), longString, "admin"));
        assertThrows(ResponseStatusException.class, () -> managedChatGroupService.createOrGetCohortGroup(longString, "name", "admin"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testGlobalConcurrent() throws Exception {
        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(1);
        
        Future<Conversation> f1 = executor.submit(() -> {
            latch.await();
            return managedChatGroupService.createOrGetGlobalGroup("Global Concurrent", "admin1");
        });
        Future<Conversation> f2 = executor.submit(() -> {
            latch.await();
            return managedChatGroupService.createOrGetGlobalGroup("Global Concurrent", "admin2");
        });
        
        latch.countDown();
        Conversation c1 = f1.get();
        Conversation c2 = f2.get();
        
        assertNotNull(c1);
        assertNotNull(c2);
        assertEquals(c1.getId(), c2.getId());
        assertEquals("Global Concurrent", c1.getName());
        executor.shutdown();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testCohortConcurrent() throws Exception {
        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(1);
        
        Future<Conversation> f1 = executor.submit(() -> {
            latch.await();
            return managedChatGroupService.createOrGetCohortGroup("cohort-cc", "Cohort Concurrent 1", "admin1");
        });
        Future<Conversation> f2 = executor.submit(() -> {
            latch.await();
            return managedChatGroupService.createOrGetCohortGroup("cohort-cc", "Cohort Concurrent 2", "admin2");
        });
        
        latch.countDown();
        Conversation c1 = f1.get();
        Conversation c2 = f2.get();
        
        assertNotNull(c1);
        assertNotNull(c2);
        assertEquals(c1.getId(), c2.getId());
        executor.shutdown();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testStructureConcurrent() throws Exception {
        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(1);
        
        Future<Conversation> f1 = executor.submit(() -> {
            latch.await();
            return managedChatGroupService.createOrGetStructureGroup("struct-cc", "Structure Concurrent 1", "admin1");
        });
        Future<Conversation> f2 = executor.submit(() -> {
            latch.await();
            return managedChatGroupService.createOrGetStructureGroup("struct-cc", "Structure Concurrent 2", "admin2");
        });
        
        latch.countDown();
        Conversation c1 = f1.get();
        Conversation c2 = f2.get();
        
        assertNotNull(c1);
        assertNotNull(c2);
        assertEquals(c1.getId(), c2.getId());
        executor.shutdown();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testReactivateConcurrent() throws Exception {
        Conversation c = managedChatGroupService.createOrGetCohortGroup("ref-react-cc", "To Reactivate CC", "admin1");
        managedChatGroupService.archiveGroup(c.getId(), "admin1");

        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(1);
        
        Future<Conversation> f1 = executor.submit(() -> {
            latch.await();
            return managedChatGroupService.reactivateGroup(c.getId(), "admin1");
        });
        Future<Conversation> f2 = executor.submit(() -> {
            latch.await();
            return managedChatGroupService.reactivateGroup(c.getId(), "admin2");
        });
        
        latch.countDown();
        int successCount = 0;
        int conflictCount = 0;
        
        try {
            f1.get();
            successCount++;
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("409"));
            conflictCount++;
        }
        
        try {
            f2.get();
            successCount++;
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("409"));
            conflictCount++;
        }
        
        assertEquals(1, successCount);
        assertEquals(1, conflictCount);
        executor.shutdown();
    }

    @Test
    public void testReactivateConflict() {
        // Create cohort 1 and archive it
        Conversation c1 = managedChatGroupService.createOrGetCohortGroup("ref-conflict", "Cohort C1", "admin1");
        managedChatGroupService.archiveGroup(c1.getId(), "admin1");
        
        // Create another active cohort with same reference
        Conversation c2 = managedChatGroupService.createOrGetCohortGroup("ref-conflict", "Cohort C2", "admin1");
        
        // Reactivating c1 should conflict
        assertThrows(ResponseStatusException.class, () -> managedChatGroupService.reactivateGroup(c1.getId(), "admin1"));
    }
}
