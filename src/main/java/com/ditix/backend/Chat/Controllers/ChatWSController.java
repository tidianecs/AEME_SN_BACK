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
import java.util.Map;

@Controller
public class ChatWSController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService profilUtilisateurCourantService;

    public ChatWSController(SimpMessagingTemplate messagingTemplate,
                            ChatService chatService,
                            com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService profilUtilisateurCourantService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.profilUtilisateurCourantService = profilUtilisateurCourantService;
    }

    private String extractSenderFullName(JwtAuthenticationToken auth) {
        Map<String, Object> claims = auth.getToken().getClaims();

        String name = (String) claims.get("name");
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }

        String givenName = (String) claims.get("given_name");
        String familyName = (String) claims.get("family_name");
        if (givenName != null || familyName != null) {
            String combined = "";
            if (givenName != null && !givenName.trim().isEmpty()) {
                combined += givenName.trim();
            }
            if (familyName != null && !familyName.trim().isEmpty()) {
                combined += (combined.isEmpty() ? "" : " ") + familyName.trim();
            }
            if (!combined.isEmpty()) {
                return combined;
            }
        }

        String preferredUsername = (String) claims.get("preferred_username");
        if (preferredUsername != null && !preferredUsername.trim().isEmpty()) {
            return preferredUsername.trim();
        }

        return "Utilisateur";
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

        com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur profil;
        try {
            profil = profilUtilisateurCourantService.obtenirProfilCourant(auth);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw new org.springframework.security.access.AccessDeniedException("Accès au profil utilisateur refusé");
        }

        String requesterUserId = profil.getKeycloakId().toString();

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

        String senderFullName = extractSenderFullName(auth);

        // Persiste et broadcast le message
        MessageDTO saved = chatService.saveMessage(
            conversationId,
            requesterUserId,
            senderFullName,
            request.getContent()
        );

        messagingTemplate.convertAndSend(
            "/topic/conversation." + conversationId,
            saved
        );
    }
}