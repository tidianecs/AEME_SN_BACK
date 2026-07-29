package com.ditix.backend.Chat;

import com.ditix.backend.Chat.Controllers.ChatWSController;
import com.ditix.backend.Chat.DTO.MessageDTO;
import com.ditix.backend.Chat.DTO.SendMessageRequest;
import com.ditix.backend.Chat.Services.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatWSControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatWSController chatWSController;

    private JwtAuthenticationToken createMockAuth(String subject) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(subject);
        JwtAuthenticationToken auth = mock(JwtAuthenticationToken.class);
        when(auth.getToken()).thenReturn(jwt);
        return auth;
    }

    @Test
    void participant_shouldSaveAndBroadcastMessage() {
        JwtAuthenticationToken auth = createMockAuth("user1");
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(1L);
        request.setContent("Hello");

        when(chatService.canAccessConversation(1L, "user1")).thenReturn(true);
        MessageDTO mockMessageDTO = new MessageDTO(1L, "user1", "Hello");
        when(chatService.saveMessage(1L, "user1", "Hello")).thenReturn(mockMessageDTO);

        assertDoesNotThrow(() -> chatWSController.sendMessage(request, auth));

        verify(chatService, times(1)).canAccessConversation(1L, "user1");
        verify(chatService, times(1)).saveMessage(1L, "user1", "Hello");
        verify(messagingTemplate, times(1)).convertAndSend("/topic/conversation.1", mockMessageDTO);
    }

    @Test
    void nonParticipant_shouldThrowAccessDenied() {
        JwtAuthenticationToken auth = createMockAuth("user3");
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(1L);
        request.setContent("Hello");

        when(chatService.canAccessConversation(1L, "user3")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> chatWSController.sendMessage(request, auth));

        verify(chatService, never()).saveMessage(any(), any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void missingPrincipal_shouldBeRejected() {
        SendMessageRequest request = new SendMessageRequest();
        
        assertThrows(AccessDeniedException.class, () -> chatWSController.sendMessage(request, null));

        verify(chatService, never()).saveMessage(any(), any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void missingConversationId_shouldBeRejected() {
        JwtAuthenticationToken auth = createMockAuth("user1");
        SendMessageRequest request = new SendMessageRequest();
        
        assertThrows(AccessDeniedException.class, () -> chatWSController.sendMessage(request, auth));

        verify(chatService, never()).canAccessConversation(any(), any());
        verify(chatService, never()).saveMessage(any(), any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void spoofedSenderId_shouldBeOverwrittenByJwtSubject() {
        JwtAuthenticationToken auth = createMockAuth("vrai-user");
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(1L);
        request.setContent("Hello");
        
        when(chatService.canAccessConversation(1L, "vrai-user")).thenReturn(true);
        MessageDTO mockMessageDTO = new MessageDTO(1L, "vrai-user", "Hello");
        when(chatService.saveMessage(1L, "vrai-user", "Hello")).thenReturn(mockMessageDTO);

        assertDoesNotThrow(() -> chatWSController.sendMessage(request, auth));

        verify(chatService, times(1)).saveMessage(1L, "vrai-user", "Hello");
    }

    @Test
    void nonexistentConversation_shouldBeRejected() {
        JwtAuthenticationToken auth = createMockAuth("user1");
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(999L);
        request.setContent("Hello");

        when(chatService.canAccessConversation(999L, "user1")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> chatWSController.sendMessage(request, auth));
    }
}
