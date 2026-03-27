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
            throw new RuntimeException("Non authentifié");
        }

        // Récupère l'ID du user depuis le JWT
        JwtAuthenticationToken auth = (JwtAuthenticationToken) principal;
        String senderId = auth.getToken().getSubject();

        // Persiste et broadcast le message
        MessageDTO saved = chatService.saveMessage(
            request.getConversationId(),
            senderId,
            request.getContent()
        );

        messagingTemplate.convertAndSend(
            "/topic/conversation." + request.getConversationId(),
            saved
        );
    }
}