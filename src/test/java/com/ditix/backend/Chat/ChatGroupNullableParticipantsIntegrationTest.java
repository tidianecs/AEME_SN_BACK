package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ChatGroupNullableParticipantsIntegrationTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Test
    public void testDirect_WithTwoIds_Success() {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.DIRECT);
        conv.setUserOneId("u1");
        conv.setUserTwoId("u2");
        conv.setActive(true);
        Conversation saved = conversationRepository.saveAndFlush(conv);
        assertNotNull(saved.getId());
    }

    @Test
    public void testDirect_WithUserOneNull_Fails() {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.DIRECT);
        conv.setUserOneId(null);
        conv.setUserTwoId("u2");
        conv.setActive(true);
        assertThrows(DataIntegrityViolationException.class, () -> {
            conversationRepository.saveAndFlush(conv);
        });
    }

    @Test
    public void testDirect_WithUserTwoBlank_Fails() {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.DIRECT);
        conv.setUserOneId("u1");
        conv.setUserTwoId("   ");
        conv.setActive(true);
        assertThrows(DataIntegrityViolationException.class, () -> {
            conversationRepository.saveAndFlush(conv);
        });
    }

    @Test
    public void testCohort_WithNullIds_Success() {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.COHORT);
        conv.setReferenceId("ref1");
        conv.setActive(true);
        Conversation saved = conversationRepository.saveAndFlush(conv);
        assertNotNull(saved.getId());
    }

    @Test
    public void testCohort_WithOneId_Fails() {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.COHORT);
        conv.setReferenceId("ref2");
        conv.setActive(true);
        conv.setUserOneId("u1");
        assertThrows(DataIntegrityViolationException.class, () -> {
            conversationRepository.saveAndFlush(conv);
        });
    }

    @Test
    public void testStructure_WithNullIds_Success() {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.STRUCTURE);
        conv.setReferenceId("struct1");
        conv.setActive(true);
        Conversation saved = conversationRepository.saveAndFlush(conv);
        assertNotNull(saved.getId());
    }

    @Test
    public void testGlobal_WithNullIds_Success() {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.GLOBAL);
        conv.setActive(true);
        Conversation saved = conversationRepository.saveAndFlush(conv);
        assertNotNull(saved.getId());
    }
}
