package com.ditix.backend.Chat;

import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


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
    @org.junit.jupiter.api.BeforeEach
    void setUp() {

        try {
            org.mockito.Mockito.lenient().when(profilUtilisateurCourantService.obtenirProfilCourant(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
                Object arg = invocation.getArgument(0);
                if (arg instanceof JwtAuthenticationToken) {
                    JwtAuthenticationToken auth = (JwtAuthenticationToken) arg;
                    String sub = auth.getToken().getSubject();
                    if (sub == null || sub.trim().isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
                    }
                    ProfilUtilisateur profil = new ProfilUtilisateur();

                    // Use a standard UUID if it's not a UUID format to prevent parsing errors elsewhere
                    try {
                        profil.setKeycloakId(UUID.fromString(sub));
                    } catch (Exception e) {
                        // fallback for tests using dummy strings like "11111111-1111-1111-1111-111111111111"
                        if ("00000000-0000-0000-0000-000000000001".equals(sub)) {
                             profil.setKeycloakId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
                        } else {
                             profil.setKeycloakId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
                        }
                    }

                    profil.setActif(true);

                    if (sub.contains("admin") || sub.contains("Admin")) {
                        profil.setRole(RoleUtilisateur.ADMIN);
                    } else if (sub.contains("dage")) {
                        profil.setRole(RoleUtilisateur.DAGE);
                    } else if (sub.contains("gestionnaire")) {
                        profil.setRole(RoleUtilisateur.GESTIONNAIRE);
                    } else {
                        profil.setRole(RoleUtilisateur.GESTIONNAIRE);
                    }

                    if (sub.contains("inactive")) {
                        profil.setActif(false);
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
                    }

                    if (sub.contains("missing_profile")) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
                    }

                    return profil;
                }
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès au profil utilisateur refusé");
            });
        } catch (Exception e) {}
    }


    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatService chatService;
    @org.mockito.Mock
    private ProfilUtilisateurCourantService profilUtilisateurCourantService;


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
        JwtAuthenticationToken auth = createMockAuth("11111111-1111-1111-1111-111111111111");
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(1L);
        request.setContent("Hello");

        when(chatService.canAccessConversation(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);
        MessageDTO mockMessageDTO = new MessageDTO(1L, "11111111-1111-1111-1111-111111111111", "Test User", "Hello");
        when(chatService.saveMessage(1L, "11111111-1111-1111-1111-111111111111", "Test User", "Hello")).thenReturn(mockMessageDTO);

        assertDoesNotThrow(() -> chatWSController.sendMessage(request, auth));

        verify(chatService, times(1)).canAccessConversation(1L, "11111111-1111-1111-1111-111111111111");
        verify(chatService, times(1)).saveMessage(1L, "11111111-1111-1111-1111-111111111111", "Test User", "Hello");
        verify(messagingTemplate, times(1)).convertAndSend("/topic/conversation.1", mockMessageDTO);
    }

    @Test
    void nonParticipant_shouldThrowAccessDenied() {
        JwtAuthenticationToken auth = createMockAuth("11111111-1111-1111-1111-111111111111");
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(1L);
        request.setContent("Hello");

        when(chatService.canAccessConversation(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(false);

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
        JwtAuthenticationToken auth = createMockAuth("11111111-1111-1111-1111-111111111111");
        SendMessageRequest request = new SendMessageRequest();

        assertThrows(AccessDeniedException.class, () -> chatWSController.sendMessage(request, auth));

        verify(chatService, never()).canAccessConversation(any(), any());
        verify(chatService, never()).saveMessage(any(), any(), any(), any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void spoofedSenderId_shouldBeOverwrittenByJwtSubject() {
        JwtAuthenticationToken auth = createMockAuth("11111111-1111-1111-1111-111111111111");
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(1L);
        request.setContent("Hello");

        when(chatService.canAccessConversation(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);
        MessageDTO mockMessageDTO = new MessageDTO(1L, "11111111-1111-1111-1111-111111111111", "Test User", "Hello");
        when(chatService.saveMessage(1L, "11111111-1111-1111-1111-111111111111", "Test User", "Hello")).thenReturn(mockMessageDTO);

        assertDoesNotThrow(() -> chatWSController.sendMessage(request, auth));

        verify(chatService, times(1)).saveMessage(1L, "11111111-1111-1111-1111-111111111111", "Test User", "Hello");
    }

    @Test
    void testSenderFullNameFallbacks() {
        // Fallback 2: given_name + family_name
        JwtAuthenticationToken auth2 = createMockAuth("11111111-1111-1111-1111-111111111111", Map.of("given_name", "Jane", "family_name", "Doe"));
        SendMessageRequest req2 = new SendMessageRequest();
        req2.setConversationId(2L);
        req2.setContent("Test");
        when(chatService.canAccessConversation(2L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);
        when(chatService.saveMessage(2L, "11111111-1111-1111-1111-111111111111", "Jane Doe", "Test")).thenReturn(new MessageDTO(2L, "11111111-1111-1111-1111-111111111111", "Jane Doe", "Test"));
        assertDoesNotThrow(() -> chatWSController.sendMessage(req2, auth2));
        verify(chatService, times(1)).saveMessage(2L, "11111111-1111-1111-1111-111111111111", "Jane Doe", "Test");

        // Fallback 3: preferred_username
        JwtAuthenticationToken auth3 = createMockAuth("11111111-1111-1111-1111-111111111111", Map.of("preferred_username", "johnd"));
        SendMessageRequest req3 = new SendMessageRequest();
        req3.setConversationId(3L);
        req3.setContent("Test");
        when(chatService.canAccessConversation(3L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);
        when(chatService.saveMessage(3L, "11111111-1111-1111-1111-111111111111", "johnd", "Test")).thenReturn(new MessageDTO(3L, "11111111-1111-1111-1111-111111111111", "johnd", "Test"));
        assertDoesNotThrow(() -> chatWSController.sendMessage(req3, auth3));
        verify(chatService, times(1)).saveMessage(3L, "11111111-1111-1111-1111-111111111111", "johnd", "Test");

        // Fallback 4: Utilisateur
        JwtAuthenticationToken auth4 = createMockAuth("11111111-1111-1111-1111-111111111111", Map.of());
        SendMessageRequest req4 = new SendMessageRequest();
        req4.setConversationId(4L);
        req4.setContent("Test");
        when(chatService.canAccessConversation(4L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);
        when(chatService.saveMessage(4L, "11111111-1111-1111-1111-111111111111", "Utilisateur", "Test")).thenReturn(new MessageDTO(4L, "11111111-1111-1111-1111-111111111111", "Utilisateur", "Test"));
        assertDoesNotThrow(() -> chatWSController.sendMessage(req4, auth4));
        verify(chatService, times(1)).saveMessage(4L, "11111111-1111-1111-1111-111111111111", "Utilisateur", "Test");
    }

    @Test
    void nonexistentConversation_shouldBeRejected() {
        JwtAuthenticationToken auth = createMockAuth("11111111-1111-1111-1111-111111111111");
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(999L);
        request.setContent("Hello");

        when(chatService.canAccessConversation(999L, "11111111-1111-1111-1111-111111111111")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> chatWSController.sendMessage(request, auth));
    }
}
