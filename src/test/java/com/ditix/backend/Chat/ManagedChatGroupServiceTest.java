package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Chat.Services.ManagedChatGroupService;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagedChatGroupServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MinistereRepository ministereRepository;

    @InjectMocks
    private ManagedChatGroupService managedChatGroupService;

    @Test
    void createOrGetMinistereGroup_NotFound() {
        when(ministereRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            managedChatGroupService.createOrGetMinistereGroup("1", "Ministere", "user1");
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(conversationRepository, never()).findByTypeAndReferenceIdAndActiveTrue(any(), anyString());
        verify(conversationRepository, never()).insertCohortStructureOrMinistereGroupAtomically(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createOrGetMinistereGroup_Inactive() {
        Ministere ministere = new Ministere();
        ministere.setId(1L);
        ministere.setActif(false);

        when(ministereRepository.findById(1L)).thenReturn(Optional.of(ministere));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            managedChatGroupService.createOrGetMinistereGroup("1", "Ministere", "user1");
        });

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
        verify(conversationRepository, never()).findByTypeAndReferenceIdAndActiveTrue(any(), anyString());
        verify(conversationRepository, never()).insertCohortStructureOrMinistereGroupAtomically(anyString(), anyString(), anyString(), anyString());
    }
}
