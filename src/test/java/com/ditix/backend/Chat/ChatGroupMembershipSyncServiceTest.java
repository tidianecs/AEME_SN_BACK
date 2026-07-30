package com.ditix.backend.Chat;

import com.ditix.backend.Chat.DTO.ChatGroupSyncReport;
import com.ditix.backend.Chat.DTO.ChatGroupUser;
import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Chat.Services.ChatGroupMembershipApplyService;
import com.ditix.backend.Chat.Services.ChatGroupMembershipSyncService;
import com.ditix.backend.Chat.Services.ChatGroupUserDirectory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ChatGroupMembershipSyncServiceTest {

    private ChatGroupUserDirectory userDirectory;
    private ChatGroupMembershipApplyService applyService;
    private ConversationRepository conversationRepository;
    private ChatGroupMembershipSyncService syncService;

    @BeforeEach
    void setUp() {
        userDirectory = mock(ChatGroupUserDirectory.class);
        applyService = mock(ChatGroupMembershipApplyService.class);
        conversationRepository = mock(ConversationRepository.class);
        syncService = new ChatGroupMembershipSyncService(userDirectory, applyService, conversationRepository);
    }

    @Test
    void testSyncGroup_Global() {
        Conversation conv = new Conversation();
        conv.setId(1L);
        conv.setType(ConversationType.GLOBAL);
        conv.setSystemManaged(true);
        conv.setActive(true);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conv));
        
        List<ChatGroupUser> users = List.of(
                new ChatGroupUser("u1", null, null),
                new ChatGroupUser("u2", null, null)
        );
        when(userDirectory.fetchAllEnabledUsers()).thenReturn(users);
        when(applyService.applyGroupSync(eq(1L), anyList())).thenReturn(new ChatGroupSyncReport());

        syncService.syncGroup(1L);

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(applyService).applyGroupSync(eq(1L), captor.capture());

        List<String> desired = captor.getValue();
        assertEquals(2, desired.size());
        assertTrue(desired.contains("u1"));
        assertTrue(desired.contains("u2"));
    }

    @Test
    void testSyncGroup_Cohort() {
        Conversation conv = new Conversation();
        conv.setId(2L);
        conv.setType(ConversationType.COHORT);
        conv.setReferenceId("COHORT-A");
        conv.setSystemManaged(true);
        conv.setActive(true);

        when(conversationRepository.findById(2L)).thenReturn(Optional.of(conv));
        
        List<ChatGroupUser> users = List.of(
                new ChatGroupUser("u1", "COHORT-A", null),
                new ChatGroupUser("u2", "COHORT-B", null)
        );
        when(userDirectory.fetchAllEnabledUsers()).thenReturn(users);

        syncService.syncGroup(2L);

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(applyService).applyGroupSync(eq(2L), captor.capture());

        List<String> desired = captor.getValue();
        assertEquals(1, desired.size());
        assertTrue(desired.contains("u1"));
    }

    @Test
    void testSyncGroup_Structure() {
        Conversation conv = new Conversation();
        conv.setId(3L);
        conv.setType(ConversationType.STRUCTURE);
        conv.setReferenceId("10");
        conv.setSystemManaged(true);
        conv.setActive(true);

        when(conversationRepository.findById(3L)).thenReturn(Optional.of(conv));
        
        List<ChatGroupUser> users = List.of(
                new ChatGroupUser("u1", null, "10"),
                new ChatGroupUser("u2", null, "11"),
                new ChatGroupUser("u3", null, null)
        );
        when(userDirectory.fetchAllEnabledUsers()).thenReturn(users);

        syncService.syncGroup(3L);

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(applyService).applyGroupSync(eq(3L), captor.capture());

        List<String> desired = captor.getValue();
        assertEquals(1, desired.size());
        assertTrue(desired.contains("u1"));
    }
}
