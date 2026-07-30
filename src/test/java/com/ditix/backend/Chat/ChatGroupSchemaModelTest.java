package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationMember;
import com.ditix.backend.Chat.Models.ConversationMemberRole;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Models.Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChatGroupSchemaModelTest {

    @Test
    void testNewConversationIsDirectAndActive() {
        Conversation conversation = new Conversation();
        assertEquals(ConversationType.DIRECT, conversation.getType());
        assertTrue(conversation.isActive());
        assertFalse(conversation.isSystemManaged());
    }

    @Test
    void testNewConversationMemberDefaults() {
        ConversationMember member = new ConversationMember();
        assertEquals(ConversationMemberRole.MEMBER, member.getRole());
        assertTrue(member.isActive());

        member.prePersist();
        assertNotNull(member.getJoinedAt());
    }

    @Test
    void testEnumValues() {
        assertEquals(4, ConversationType.values().length);
        assertDoesNotThrow(() -> ConversationType.valueOf("DIRECT"));
        assertDoesNotThrow(() -> ConversationType.valueOf("COHORT"));
        assertDoesNotThrow(() -> ConversationType.valueOf("STRUCTURE"));
        assertDoesNotThrow(() -> ConversationType.valueOf("GLOBAL"));

        assertEquals(2, ConversationMemberRole.values().length);
        assertDoesNotThrow(() -> ConversationMemberRole.valueOf("MEMBER"));
        assertDoesNotThrow(() -> ConversationMemberRole.valueOf("GROUP_ADMIN"));
    }

    @Test
    void testMessageSenderFullName() {
        Message message = new Message();
        assertNull(message.getSenderFullName());

        message.setSenderFullName("John Doe");
        assertEquals("John Doe", message.getSenderFullName());
    }
}
