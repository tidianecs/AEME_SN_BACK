package com.ditix.backend.Chat.Security;

import com.ditix.backend.Chat.Services.ChatService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Componen
public class ChatWebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final ChatService chatService;
    private final com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService profilUtilisateurCourantService;
    private static final Pattern CONVERSATION_TOPIC_PATTERN = Pattern.compile("^/topic/conversation\\.(\\d+)$");
    private static final String CONVERSATION_PREFIX = "/topic/conversation.";

    public ChatWebSocketChannelInterceptor(JwtDecoder jwtDecoder, ChatService chatService, com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService profilUtilisateurCourantService) {
        this.jwtDecoder = jwtDecoder;
        this.chatService = chatService;
        this.profilUtilisateurCourantService = profilUtilisateurCourantService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        switch (command) {
            case CONNECT:
                handleConnect(accessor);
                break;
            case SUBSCRIBE:
                handleSubscribe(accessor);
                break;
            case SEND:
                handleSend(accessor);
                break;
            default:
                break;
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null) {
            throw new AccessDeniedException("Authentication required");
        }
        if (!authHeader.startsWith("Bearer ")) {
            throw new AccessDeniedException("Invalid authentication token");
        }

        String token = authHeader.substring(7);
        if (token.trim().isEmpty()) {
            throw new AccessDeniedException("Invalid authentication token");
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);
            String subject = jwt.getSubject();
            if (subject == null || subject.trim().isEmpty()) {
                throw new AccessDeniedException("Invalid authentication token");
            }
            JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);

            // Check postgres profile
            profilUtilisateurCourantService.obtenirProfilCourant(auth);

            accessor.setUser(auth);
        } catch (Exception e) {
            throw new AccessDeniedException("Invalid authentication token");
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        if (destination.startsWith(CONVERSATION_PREFIX)) {
            Matcher matcher = CONVERSATION_TOPIC_PATTERN.matcher(destination);
            if (!matcher.matches()) {
                throw new AccessDeniedException("Access denied");
            }

            Principal principal = accessor.getUser();
            if (!(principal instanceof JwtAuthenticationToken)) {
                throw new AccessDeniedException("Access denied");
            }

            JwtAuthenticationToken auth = (JwtAuthenticationToken) principal;

            com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur profil;
            try {
                profil = profilUtilisateurCourantService.obtenirProfilCourant(auth);
            } catch (Exception e) {
                throw new AccessDeniedException("Access denied");
            }

            String userId = profil.getKeycloakId().toString();

            try {
                Long conversationId = Long.parseLong(matcher.group(1));
                if (!chatService.canAccessConversation(conversationId, userId)) {
                    throw new AccessDeniedException("Access denied");
                }
            } catch (NumberFormatException e) {
                throw new AccessDeniedException("Access denied");
            }
        }
    }

    private void handleSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination != null && destination.startsWith(CONVERSATION_PREFIX)) {
            throw new AccessDeniedException("Direct broker publishing is not allowed");
        }
    }
}
