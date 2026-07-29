package com.ditix.backend.Chat.Controllers;

import com.ditix.backend.Chat.DTO.MessageDTO;
import com.ditix.backend.Chat.DTO.SendMessageRequest;
import com.ditix.backend.Chat.Services.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
public class ChatWSController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    public ChatWSController(SimpMessagingTemplate messagingTemplate,
                            ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request,
                            Principal principal) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("Non authentifié");
        }

        if (!(principal instanceof JwtAuthenticationToken)) {
            throw new org.springframework.security.access.AccessDeniedException("Type d'authentification invalide");
        }

        JwtAuthenticationToken auth = (JwtAuthenticationToken) principal;
        String requesterUserId = auth.getToken().getSubject();

        if (requesterUserId == null || requesterUserId.trim().isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("ID utilisateur manquant");
        }

        Long conversationId = request.getConversationId();
        if (conversationId == null) {
            throw new org.springframework.security.access.AccessDeniedException("ID de conversation manquant");
        }

        if (!chatService.canAccessConversation(conversationId, requesterUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("Accès refusé à cette conversation");
        }

        // Persiste et broadcast le message
        MessageDTO saved = chatService.saveMessage(
            conversationId,
            requesterUserId,
            request.getContent()
        );

        messagingTemplate.convertAndSend(
            "/topic/conversation." + conversationId,
            saved
        );
    }
}