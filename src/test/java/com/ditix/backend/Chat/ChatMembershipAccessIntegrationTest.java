package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationMember;
import com.ditix.backend.Chat.Models.ConversationMemberRole;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationMemberRepository;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ChatMembershipAccessIntegrationTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    private Conversation createConversation(ConversationType type, boolean active) {
        Conversation c = new Conversation();
        c.setType(type);
        c.setActive(active);
        c.setUserOneId("dummy1");
        c.setUserTwoId("dummy2");
        return conversationRepository.save(c);
    }

    private ConversationMember createMember(Long conversationId, String userId, boolean active) {
        ConversationMember cm = new ConversationMember();
        cm.setConversationId(conversationId);
        cm.setUserId(userId);
        cm.setActive(active);
        cm.setRole(ConversationMemberRole.MEMBER);
        cm.setJoinedAt(LocalDateTime.now());
        return conversationMemberRepository.save(cm);
    }

    @Test
    public void testFindAllByUserId_accessRules() {
        String testUser = "target_user";

        // 1. DIRECT active with active member
        Conversation c1 = createConversation(ConversationType.DIRECT, true);
        createMember(c1.getId(), testUser, true);

        // 2. COHORT active with active member
        Conversation c2 = createConversation(ConversationType.COHORT, true);
        createMember(c2.getId(), testUser, true);

        // 3. STRUCTURE active with active member
        Conversation c3 = createConversation(ConversationType.STRUCTURE, true);
        createMember(c3.getId(), testUser, true);

        // 4. GLOBAL active with active member
        Conversation c4 = createConversation(ConversationType.GLOBAL, true);
        createMember(c4.getId(), testUser, true);

        // 5. adhésion inactive exclue
        Conversation c5 = createConversation(ConversationType.DIRECT, true);
        createMember(c5.getId(), testUser, false);

        // 6. conversation inactive exclue
        Conversation c6 = createConversation(ConversationType.DIRECT, false);
        createMember(c6.getId(), testUser, true);

        // 7. non-membre exclu
        Conversation c7 = createConversation(ConversationType.DIRECT, true);
        createMember(c7.getId(), "other_user", true);

        // 8. historique d'adhésion inactive plus adhésion active pour la même conversation
        Conversation c8 = createConversation(ConversationType.DIRECT, true);
        createMember(c8.getId(), testUser, false);
        createMember(c8.getId(), testUser, true);

        List<Conversation> results = conversationRepository.findAllByUserId(testUser);

        // Check total (1, 2, 3, 4, and 8) => 5 conversations. No duplicates.
        assertEquals(5, results.size());

        assertTrue(results.stream().anyMatch(c -> c.getId().equals(c1.getId())));
        assertTrue(results.stream().anyMatch(c -> c.getId().equals(c2.getId())));
        assertTrue(results.stream().anyMatch(c -> c.getId().equals(c3.getId())));
        assertTrue(results.stream().anyMatch(c -> c.getId().equals(c4.getId())));
        assertTrue(results.stream().anyMatch(c -> c.getId().equals(c8.getId())));

        assertFalse(results.stream().anyMatch(c -> c.getId().equals(c5.getId())));
        assertFalse(results.stream().anyMatch(c -> c.getId().equals(c6.getId())));
        assertFalse(results.stream().anyMatch(c -> c.getId().equals(c7.getId())));

        // Check ordering: c8 should be first (createdAt desc)
        assertEquals(c8.getId(), results.get(0).getId());
    }
}
