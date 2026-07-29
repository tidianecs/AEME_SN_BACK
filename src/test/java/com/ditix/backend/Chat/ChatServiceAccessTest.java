package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Chat.Services.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceAccessTest {

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private ChatService chatService;

    private Conversation createMockConversation(Long id, String userOneId, String userTwoId) {
        Conversation conv = new Conversation();
        conv.setId(id);
        conv.setUserOneId(userOneId);
        conv.setUserTwoId(userTwoId);
        return conv;
    }

    @Test
    void canAccessConversation_userOne_shouldReturnTrue() {
        Conversation conv = createMockConversation(1L, "user1", "user2");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));

        assertTrue(chatService.canAccessConversation(1L, "user1"));
    }

    @Test
    void canAccessConversation_userTwo_shouldReturnTrue() {
        Conversation conv = createMockConversation(1L, "user1", "user2");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));

        assertTrue(chatService.canAccessConversation(1L, "user2"));
    }

    @Test
    void canAccessConversation_nonParticipant_shouldReturnFalse() {
        Conversation conv = createMockConversation(1L, "user1", "user2");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));

        assertFalse(chatService.canAccessConversation(1L, "user3"));
    }

    @Test
    void canAccessConversation_missingConversation_shouldReturnFalse() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.empty());

        assertFalse(chatService.canAccessConversation(1L, "user1"));
    }

    @Test
    void canAccessConversation_nullConversationId_shouldReturnFalse() {
        assertFalse(chatService.canAccessConversation(null, "user1"));
    }

    @Test
    void canAccessConversation_nullUserId_shouldReturnFalse() {
        assertFalse(chatService.canAccessConversation(1L, null));
    }

    @Test
    void canAccessConversation_blankUserId_shouldReturnFalse() {
        assertFalse(chatService.canAccessConversation(1L, "   "));
    }

    @Test
    void canAccessConversation_nullParticipantFields_shouldNotThrow() {
        Conversation conv = createMockConversation(1L, null, null);
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));

        assertFalse(chatService.canAccessConversation(1L, "user1"));
    }
}
