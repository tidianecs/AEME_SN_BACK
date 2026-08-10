package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Chat.Services.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationMemberRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceAccessTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMemberRepository conversationMemberRepository;

    @Mock
    private com.ditix.backend.Chat.Repository.MessageRepository messageRepository;

    @InjectMocks
    private ChatService chatService;

    private Conversation createMockConversation(Long id, String userOneId, String userTwoId) {
        Conversation conv = new Conversation();
        conv.setId(id);
        conv.setUserOneId(userOneId);
        conv.setUserTwoId(userTwoId);
        conv.setActive(true);
        conv.setType(ConversationType.DIRECT);
        return conv;
    }

    @Test
    void canAccessConversation_activeMember_shouldReturnTrue() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);

        assertTrue(chatService.canAccessConversation(1L, "11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void canAccessConversation_inactiveMember_shouldReturnFalse() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(false);

        assertFalse(chatService.canAccessConversation(1L, "11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void canAccessConversation_inactiveConversation_shouldReturnFalse() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        conv.setActive(false);
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));

        assertFalse(chatService.canAccessConversation(1L, "11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void canAccessConversation_missingConversation_shouldReturnFalse() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.empty());

        assertFalse(chatService.canAccessConversation(1L, "11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void canAccessConversation_nullConversationId_shouldReturnFalse() {
        assertFalse(chatService.canAccessConversation(null, "11111111-1111-1111-1111-111111111111"));
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
    void getCounterpartUserId_userOne_shouldReturnUserTwo() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);

        assertEquals("11111111-1111-1111-1111-111111111111", chatService.getCounterpartUserId(1L, "11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void getCounterpartUserId_userTwo_shouldReturnUserOne() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);

        assertEquals("11111111-1111-1111-1111-111111111111", chatService.getCounterpartUserId(1L, "11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void getCounterpartUserId_nonParticipant_shouldThrow403() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(false);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.getCounterpartUserId(1L, "11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void getCounterpartUserId_missingConversation_shouldThrow404() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.getCounterpartUserId(1L, "11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void getCounterpartUserId_nullConversationId_shouldThrow403() {
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.getCounterpartUserId(null, "11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void getCounterpartUserId_nullRequester_shouldThrow403() {
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.getCounterpartUserId(1L, null));
    }

    @Test
    void getCounterpartUserId_blankRequester_shouldThrow403() {
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.getCounterpartUserId(1L, "   "));
    }

    @Test
    void getCounterpartUserId_blankCounterpart_shouldThrow403() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "   ");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.getCounterpartUserId(1L, "11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void getCounterpartUserId_nonDirect_shouldThrow400() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        conv.setType(ConversationType.GLOBAL);
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.getCounterpartUserId(1L, "11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void getOrCreateConversation_newConversationDirect_shouldSaveAndEnsureMembers() {
        when(conversationRepository.findBetweenUsers("u1", "u2")).thenReturn(Optional.empty());
        Conversation savedConv = createMockConversation(10L, "u1", "u2");
        when(conversationRepository.save(any(Conversation.class))).thenReturn(savedConv);

        Conversation result = chatService.getOrCreateConversation("u1", "u2");

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(conversationRepository).save(any(Conversation.class));
        verify(conversationMemberRepository).insertActiveMemberIfAbsent(10L, "u1");
        verify(conversationMemberRepository).insertActiveMemberIfAbsent(10L, "u2");
    }

    @Test
    void getOrCreateConversation_existingConversationDirect_shouldNotSaveButEnsureMembers() {
        Conversation existing = createMockConversation(10L, "u1", "u2");
        when(conversationRepository.findBetweenUsers("u1", "u2")).thenReturn(Optional.of(existing));

        Conversation result = chatService.getOrCreateConversation("u1", "u2");

        assertEquals(10L, result.getId());
        verify(conversationRepository, never()).save(any(Conversation.class));
        verify(conversationMemberRepository).insertActiveMemberIfAbsent(10L, "u1");
        verify(conversationMemberRepository).insertActiveMemberIfAbsent(10L, "u2");
    }

    @Test
    void getOrCreateConversation_selfConversation_shouldInsertOnlyOneMember() {
        Conversation existing = createMockConversation(10L, "u1", "u1");
        when(conversationRepository.findBetweenUsers("u1", "u1")).thenReturn(Optional.of(existing));

        chatService.getOrCreateConversation("u1", "u1");

        verify(conversationMemberRepository, times(1)).insertActiveMemberIfAbsent(10L, "u1");
    }

    @Test
    void getOrCreateConversation_blankUserId_shouldNotInsertForThatId() {
        Conversation existing = createMockConversation(10L, "u1", "  ");
        when(conversationRepository.findBetweenUsers("u1", "  ")).thenReturn(Optional.of(existing));

        chatService.getOrCreateConversation("u1", "  ");

        verify(conversationMemberRepository).insertActiveMemberIfAbsent(10L, "u1");
        verify(conversationMemberRepository, never()).insertActiveMemberIfAbsent(10L, "  ");
    }

    @Test
    void getOrCreateConversation_nonDirect_shouldNotInsertMembers() {
        Conversation existing = createMockConversation(10L, "u1", "u2");
        existing.setType(ConversationType.GLOBAL);
        when(conversationRepository.findBetweenUsers("u1", "u2")).thenReturn(Optional.of(existing));

        chatService.getOrCreateConversation("u1", "u2");

        verify(conversationMemberRepository, never()).insertActiveMemberIfAbsent(any(), any());
    }

    @Test
    void getOrCreateConversation_duplicateInsertion_shouldContinueNormally() {
        Conversation existing = createMockConversation(10L, "u1", "u2");
        when(conversationRepository.findBetweenUsers("u1", "u2")).thenReturn(Optional.of(existing));
        when(conversationMemberRepository.insertActiveMemberIfAbsent(10L, "u1")).thenReturn(0);
        when(conversationMemberRepository.insertActiveMemberIfAbsent(10L, "u2")).thenReturn(1);

        assertDoesNotThrow(() -> chatService.getOrCreateConversation("u1", "u2"));

        verify(conversationMemberRepository).insertActiveMemberIfAbsent(10L, "u1");
        verify(conversationMemberRepository).insertActiveMemberIfAbsent(10L, "u2");
    }

    @Test
    void deleteConversation_directActiveMember_shouldDelete() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);

        assertDoesNotThrow(() -> chatService.deleteConversation(1L, "11111111-1111-1111-1111-111111111111"));

        verify(conversationRepository).delete(conv);
    }

    @Test
    void deleteConversation_directNonMember_shouldThrow403() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(false);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.deleteConversation(1L, "11111111-1111-1111-1111-111111111111"));
        verify(conversationRepository, never()).delete(any());
    }

    @Test
    void deleteConversation_directInactiveMember_shouldThrow403() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(false);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.deleteConversation(1L, "11111111-1111-1111-1111-111111111111"));
        verify(conversationRepository, never()).delete(any());
    }

    @Test
    void deleteConversation_inactiveConversation_shouldThrow403() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        conv.setActive(false);
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.deleteConversation(1L, "11111111-1111-1111-1111-111111111111"));
        verify(conversationRepository, never()).delete(any());
    }

    @Test
    void deleteConversation_missingConversation_shouldThrow404() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.deleteConversation(1L, "11111111-1111-1111-1111-111111111111"));
        verify(conversationRepository, never()).delete(any());
    }

    @Test
    void deleteConversation_cohortActiveMember_shouldThrow403() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        conv.setType(ConversationType.COHORT);
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.deleteConversation(1L, "11111111-1111-1111-1111-111111111111"));
        verify(conversationRepository, never()).delete(any());
    }

    @Test
    void deleteConversation_structureActiveMember_shouldThrow403() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        conv.setType(ConversationType.STRUCTURE);
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.deleteConversation(1L, "11111111-1111-1111-1111-111111111111"));
        verify(conversationRepository, never()).delete(any());
    }

    @Test
    void deleteConversation_globalActiveMember_shouldThrow403() {
        Conversation conv = createMockConversation(1L, "11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111111");
        conv.setType(ConversationType.GLOBAL);
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> chatService.deleteConversation(1L, "11111111-1111-1111-1111-111111111111"));
        verify(conversationRepository, never()).delete(any());
    }
}
