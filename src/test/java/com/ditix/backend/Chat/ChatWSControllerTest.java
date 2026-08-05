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
import java.util.Map;

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
        return createMockAuth(subject, Map.of("name", "Test User"));
    }

    private JwtAuthenticationToken createMockAuth(String subject, Map<String, Object> claims) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(subject);
        lenient().when(jwt.getClaims()).thenReturn(claims);
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
        MessageDTO mockMessageDTO = new MessageDTO(1L, "user1", "Test User", "Hello");
        when(chatService.saveMessage(1L, "user1", "Test User", "Hello")).thenReturn(mockMessageDTO);

        assertDoesNotThrow(() -> chatWSController.sendMessage(request, auth));

        verify(chatService, times(1)).canAccessConversation(1L, "user1");
        verify(chatService, times(1)).saveMessage(1L, "user1", "Test User", "Hello");
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

        verify(chatService, never()).saveMessage(any(), any(), any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void missingPrincipal_shouldBeRejected() {
        SendMessageRequest request = new SendMessageRequest();
        
        assertThrows(AccessDeniedException.class, () -> chatWSController.sendMessage(request, null));

        verify(chatService, never()).saveMessage(any(), any(), any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void missingConversationId_shouldBeRejected() {
        JwtAuthenticationToken auth = createMockAuth("user1");
        SendMessageRequest request = new SendMessageRequest();
        
        assertThrows(AccessDeniedException.class, () -> chatWSController.sendMessage(request, auth));

        verify(chatService, never()).canAccessConversation(any(), any());
        verify(chatService, never()).saveMessage(any(), any(), any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void spoofedSenderId_shouldBeOverwrittenByJwtSubject() {
        JwtAuthenticationToken auth = createMockAuth("vrai-user");
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(1L);
        request.setContent("Hello");
        
        when(chatService.canAccessConversation(1L, "vrai-user")).thenReturn(true);
        MessageDTO mockMessageDTO = new MessageDTO(1L, "vrai-user", "Test User", "Hello");
        when(chatService.saveMessage(1L, "vrai-user", "Test User", "Hello")).thenReturn(mockMessageDTO);

        assertDoesNotThrow(() -> chatWSController.sendMessage(request, auth));

        verify(chatService, times(1)).saveMessage(1L, "vrai-user", "Test User", "Hello");
    }

    @Test
    void testSenderFullNameFallbacks() {
        // Fallback 2: given_name + family_name
        JwtAuthenticationToken auth2 = createMockAuth("user2", Map.of("given_name", "Jane", "family_name", "Doe"));
        SendMessageRequest req2 = new SendMessageRequest();
        req2.setConversationId(2L);
        req2.setContent("Test");
        when(chatService.canAccessConversation(2L, "user2")).thenReturn(true);
        when(chatService.saveMessage(2L, "user2", "Jane Doe", "Test")).thenReturn(new MessageDTO(2L, "user2", "Jane Doe", "Test"));
        assertDoesNotThrow(() -> chatWSController.sendMessage(req2, auth2));
        verify(chatService, times(1)).saveMessage(2L, "user2", "Jane Doe", "Test");

        // Fallback 3: preferred_username
        JwtAuthenticationToken auth3 = createMockAuth("user3", Map.of("preferred_username", "johnd"));
        SendMessageRequest req3 = new SendMessageRequest();
        req3.setConversationId(3L);
        req3.setContent("Test");
        when(chatService.canAccessConversation(3L, "user3")).thenReturn(true);
        when(chatService.saveMessage(3L, "user3", "johnd", "Test")).thenReturn(new MessageDTO(3L, "user3", "johnd", "Test"));
        assertDoesNotThrow(() -> chatWSController.sendMessage(req3, auth3));
        verify(chatService, times(1)).saveMessage(3L, "user3", "johnd", "Test");

        // Fallback 4: Utilisateur
        JwtAuthenticationToken auth4 = createMockAuth("user4", Map.of());
        SendMessageRequest req4 = new SendMessageRequest();
        req4.setConversationId(4L);
        req4.setContent("Test");
        when(chatService.canAccessConversation(4L, "user4")).thenReturn(true);
        when(chatService.saveMessage(4L, "user4", "Utilisateur", "Test")).thenReturn(new MessageDTO(4L, "user4", "Utilisateur", "Test"));
        assertDoesNotThrow(() -> chatWSController.sendMessage(req4, auth4));
        verify(chatService, times(1)).saveMessage(4L, "user4", "Utilisateur", "Test");
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
