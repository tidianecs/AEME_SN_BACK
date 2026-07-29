package com.ditix.backend.Chat;

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

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private ChatService chatService;

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

    @Test
    void connect_withoutAuthorization_shouldBeRejected() {
        Message<?> message = createMessage(StompCommand.CONNECT);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void connect_withMalformedAuthorization_shouldBeRejected() {
        Message<?> message = createMessageWithHeader(StompCommand.CONNECT, "Authorization", "Basic token");
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void connect_withEmptyBearerToken_shouldBeRejected() {
        Message<?> message = createMessageWithHeader(StompCommand.CONNECT, "Authorization", "Bearer ");
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void connect_withInvalidToken_shouldBeRejected() {
        Message<?> message = createMessageWithHeader(StompCommand.CONNECT, "Authorization", "Bearer invalid");
        when(jwtDecoder.decode("invalid")).thenThrow(new RuntimeException("Invalid"));
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void connect_withValidToken_shouldSetJwtPrincipal() {
        Message<?> message = createMessageWithHeader(StompCommand.CONNECT, "Authorization", "Bearer valid");
        Jwt jwt = Jwt.withTokenValue("valid").header("alg", "none").claim("sub", "user1").build();
        when(jwtDecoder.decode("valid")).thenReturn(jwt);

        Message<?> result = interceptor.preSend(message, messageChannel);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        assertNotNull(accessor.getUser());
        assertTrue(accessor.getUser() instanceof JwtAuthenticationToken);
        assertEquals("user1", ((JwtAuthenticationToken) accessor.getUser()).getToken().getSubject());
    }

    @Test
    void connect_withMissingSubject_shouldBeRejected() {
        Message<?> message = createMessageWithHeader(StompCommand.CONNECT, "Authorization", "Bearer valid");
        Jwt jwt = Jwt.withTokenValue("valid").header("alg", "none").claim("other", "value").build();
        when(jwtDecoder.decode("valid")).thenReturn(jwt);

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    // SUBSCRIBE

    @Test
    void subscribe_withoutPrincipal_shouldBeRejected() {
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.1", null);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void subscribe_participant_shouldBeAllowed() {
        JwtAuthenticationToken auth = createMockAuth("user1");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.1", auth);
        
        when(chatService.canAccessConversation(1L, "user1")).thenReturn(true);

        assertDoesNotThrow(() -> interceptor.preSend(message, messageChannel));
        verify(chatService).canAccessConversation(1L, "user1");
    }

    @Test
    void subscribe_nonParticipant_shouldBeRejected() {
        JwtAuthenticationToken auth = createMockAuth("user2");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.1", auth);
        
        when(chatService.canAccessConversation(1L, "user2")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void subscribe_missingConversation_shouldBeRejected() {
        JwtAuthenticationToken auth = createMockAuth("user1");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.99", auth);
        
        when(chatService.canAccessConversation(99L, "user1")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void subscribe_malformedConversationDestination_shouldBeRejected() {
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.abc", null);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void subscribe_unrelatedAuthenticatedDestination_shouldBeAllowed() {
        JwtAuthenticationToken auth = createMockAuth("user1");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/other.topic", auth);
        assertDoesNotThrow(() -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void subscribe_withConversationIdAboveLongMax_shouldBeRejected() {
        JwtAuthenticationToken auth = createMockAuth("user1");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.999999999999999999999999999999999999", auth);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
        verify(chatService, never()).canAccessConversation(anyLong(), anyString());
    }

    @Test
    void subscribe_withNullDestination_shouldPassThrough() {
        JwtAuthenticationToken auth = createMockAuth("user1");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, null, auth);
        assertDoesNotThrow(() -> interceptor.preSend(message, messageChannel));
        verify(chatService, never()).canAccessConversation(anyLong(), anyString());
    }

    @Test
    void subscribe_withConversationSuffix_shouldBeRejected() {
        JwtAuthenticationToken auth = createMockAuth("user1");
        Message<?> message = createMessageWithDestination(StompCommand.SUBSCRIBE, "/topic/conversation.1/extra", auth);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
        verify(chatService, never()).canAccessConversation(anyLong(), anyString());
    }

    // SEND

    @Test
    void send_directlyToConversationTopic_shouldBeRejected() {
        Message<?> message = createMessageWithDestination(StompCommand.SEND, "/topic/conversation.1", null);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void send_toMalformedConversationTopic_shouldBeRejected() {
        Message<?> message = createMessageWithDestination(StompCommand.SEND, "/topic/conversation.abc", null);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void send_toApplicationChatDestination_shouldBeAllowed() {
        Message<?> message = createMessageWithDestination(StompCommand.SEND, "/app/chat.send", null);
        assertDoesNotThrow(() -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void send_directlyToConversationTopicWithSuffix_shouldBeRejected() {
        Message<?> message = createMessageWithDestination(StompCommand.SEND, "/topic/conversation.1/extra", null);
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, messageChannel));
        verify(chatService, never()).canAccessConversation(anyLong(), anyString());
    }

    // AUTRES COMMANDES

    @Test
    void disconnect_shouldPassThrough() {
        Message<?> message = createMessage(StompCommand.DISCONNECT);
        assertDoesNotThrow(() -> interceptor.preSend(message, messageChannel));
    }

    @Test
    void nullCommand_shouldPassThrough() {
        org.springframework.messaging.support.MessageHeaderAccessor accessor = new org.springframework.messaging.support.MessageHeaderAccessor();
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        assertDoesNotThrow(() -> interceptor.preSend(message, messageChannel));
    }
}
