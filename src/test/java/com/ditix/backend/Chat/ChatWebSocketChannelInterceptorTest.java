package com.ditix.backend.Chat;

import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import com.ditix.backend.Chat.Security.ChatWebSocketChannelInterceptor;
import com.ditix.backend.Chat.Services.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatWebSocketChannelInterceptorTest {
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
    private JwtDecoder jwtDecoder;

    @Mock
    private ChatService chatService;
    @org.mockito.Mock
    private ProfilUtilisateurCourantService profilUtilisateurCourantService;


    @Mock
    private MessageChannel messageChannel;

    @InjectMocks
    private ChatWebSocketChannelInterceptor interceptor;

    private Message<?> createMessage(StompCommand command) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<?> createMessageWithHeader(StompCommand command, String headerName, String headerValue) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setNativeHeader(headerName, headerValue);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<?> createMessageWithDestination(StompCommand command, String destination, JwtAuthenticationToken auth) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setDestination(destination);
        if (auth != null) {
            accessor.setUser(auth);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private JwtAuthenticationToken createMockAuth(String subject) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(subject);
        return new JwtAuthenticationToken(jwt);
    }

    // CONNECT

    @Tes
    void connect_withoutAuthorization_shouldBeRejected() {
        Message<?> message = createMessage(StompCommand.CONNECT);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Tes
    void connect_withMalformedAuthorization_shouldBeRejected() {
        Message<?> message = createMessageWithHeader(StompCommand.CONNECT, "Authorization", "Basic token");
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Tes
    void connect_withEmptyBearerToken_shouldBeRejected() {
        Message<?> message = createMessageWithHeader(StompCommand.CONNECT, "Authorization", "Bearer ");
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Tes
    void connect_withInvalidToken_shouldBeRejected() {
        Message<?> message = createMessageWithHeader(StompCommand.CONNECT, "Authorization", "Bearer invalid");
        when(jwtDecoder.decode("invalid")).thenThrow(new RuntimeException("Invalid"));
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Tes
    void connect_withValidToken_shouldSetJwtPrincipal() {
        Message<?> message = createMessageWithHeader(StompCommand.CONNECT, "Authorization", "Bearer valid");
        Jwt jwt = Jwt.withTokenValue("valid").header("alg", "none").claim("sub", "11111111-1111-1111-1111-111111111111").build();
        when(jwtDecoder.decode("valid")).thenReturn(jwt);

        Message<?> result = interceptor.preSend(message, messageChannel);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        assertNotNull(accessor.getUser());
        assertTrue(accessor.getUser() instanceof JwtAuthenticationToken);
        assertEquals("11111111-1111-1111-1111-111111111111", ((JwtAuthenticationToken) accessor.getUser()).getToken().getSubject());
    }

    @Tes
    void connect_withMissingSubject_shouldBeRejected() {
        Message<?> message = createMessageWithHeader(StompCommand.CONNECT, "Authorization", "Bearer valid");
        Jwt jwt = Jwt.withTokenValue("valid").header("alg", "none").claim("other", "value").build();
        when(jwtDecoder.decode("valid")).thenReturn(jwt);

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    // SUBSCRIBE

    @Tes
    void subscribe_withoutPrincipal_shouldBeRejected() {
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.1", null);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Tes
    void subscribe_participant_shouldBeAllowed() {
        JwtAuthenticationToken auth = createMockAuth("11111111-1111-1111-1111-111111111111");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.1", auth);

        when(chatService.canAccessConversation(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(true);

        assertDoesNotThrow(() -> interceptor.preSend(message, messageChannel));
        verify(chatService).canAccessConversation(1L, "11111111-1111-1111-1111-111111111111");
    }

    @Tes
    void subscribe_nonParticipant_shouldBeRejected() {
        JwtAuthenticationToken auth = createMockAuth("11111111-1111-1111-1111-111111111111");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.1", auth);

        when(chatService.canAccessConversation(1L, "11111111-1111-1111-1111-111111111111")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Tes
    void subscribe_missingConversation_shouldBeRejected() {
        JwtAuthenticationToken auth = createMockAuth("11111111-1111-1111-1111-111111111111");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.99", auth);

        when(chatService.canAccessConversation(99L, "11111111-1111-1111-1111-111111111111")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Tes
    void subscribe_malformedConversationDestination_shouldBeRejected() {
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.abc", null);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Tes
    void subscribe_unrelatedAuthenticatedDestination_shouldBeAllowed() {
        JwtAuthenticationToken auth = createMockAuth("11111111-1111-1111-1111-111111111111");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/other.topic", auth);
        assertDoesNotThrow(() -> interceptor.preSend(message, messageChannel));
    }

    @Tes
    void subscribe_withConversationIdAboveLongMax_shouldBeRejected() {
        JwtAuthenticationToken auth = createMockAuth("11111111-1111-1111-1111-111111111111");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.999999999999999999999999999999999999", auth);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
        verify(chatService, never()).canAccessConversation(anyLong(), anyString());
    }

    @Tes
    void subscribe_withNullDestination_shouldPassThrough() {
        JwtAuthenticationToken auth = createMockAuth("11111111-1111-1111-1111-111111111111");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, null, auth);
        assertDoesNotThrow(() -> interceptor.preSend(message, messageChannel));
        verify(chatService, never()).canAccessConversation(anyLong(), anyString());
    }

    @Tes
    void subscribe_withConversationSuffix_shouldBeRejected() {
        JwtAuthenticationToken auth = createMockAuth("11111111-1111-1111-1111-111111111111");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.1/extra", auth);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
        verify(chatService, never()).canAccessConversation(anyLong(), anyString());
    }

    // SEND

    @Tes
    void send_directlyToConversationTopic_shouldBeRejected() {
        Message<?> message = createMessageWithDestination(StompCommand.SEND, "/topic/conversation.1", null);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Tes
    void send_toMalformedConversationTopic_shouldBeRejected() {
        Message<?> message = createMessageWithDestination(StompCommand.SEND, "/topic/conversation.abc", null);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Tes
    void send_toApplicationChatDestination_shouldBeAllowed() {
        Message<?> message = createMessageWithDestination(StompCommand.SEND, "/app/chat.send", null);
        assertDoesNotThrow(() -> interceptor.preSend(message, messageChannel));
    }

    @Tes
    void send_directlyToConversationTopicWithSuffix_shouldBeRejected() {
        Message<?> message = createMessageWithDestination(StompCommand.SEND, "/topic/conversation.1/extra", null);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
        verify(chatService, never()).canAccessConversation(anyLong(), anyString());
    }

    // AUTRES COMMANDES

    @Tes
    void disconnect_shouldPassThrough() {
        Message<?> message = createMessage(StompCommand.DISCONNECT);
        assertDoesNotThrow(() -> interceptor.preSend(message, messageChannel));
    }

    @Tes
    void nullCommand_shouldPassThrough() {
        org.springframework.messaging.support.MessageHeaderAccessor accessor = new org.springframework.messaging.support.MessageHeaderAccessor();
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        assertDoesNotThrow(() -> interceptor.preSend(message, messageChannel));
    }
}
