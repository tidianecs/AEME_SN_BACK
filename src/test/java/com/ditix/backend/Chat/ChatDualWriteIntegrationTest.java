package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationMember;
import com.ditix.backend.Chat.Services.ChatService;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Chat.Repository.ConversationMemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ChatDualWriteIntegrationTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    @Test
    public void testDualWriteIntegration() {
        String u1 = "int_user1";
        String u2 = "int_user2";

        // Appel 1 : Création
        Conversation conv1 = chatService.getOrCreateConversation(u1, u2);
        assertNotNull(conv1.getId());
        assertEquals(u1, conv1.getUserOneId());
        assertEquals(u2, conv1.getUserTwoId());

        List<ConversationMember> members = conversationMemberRepository.findByConversationIdAndActiveTrue(conv1.getId());
        assertEquals(2, members.size());

        // Appel 2 : Même paire
        Conversation conv2 = chatService.getOrCreateConversation(u1, u2);
        assertEquals(conv1.getId(), conv2.getId());

        List<ConversationMember> members2 = conversationMemberRepository.findByConversationIdAndActiveTrue(conv1.getId());
        assertEquals(2, members2.size());
    }
}
