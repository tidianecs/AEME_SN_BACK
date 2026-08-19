package com.ditix.backend.Chat.Controllers;

import com.ditix.backend.Chat.DTO.MessageDTO;
import com.ditix.backend.Chat.Services.ChatService;
import com.ditix.backend.Chat.Models.Conversation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatRESTController {

    private final ChatService chatService;
    private final com.ditix.backend.Auth.Services.AuthService authService;
    private final com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService profilUtilisateurCourantService;
    private final com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository profilUtilisateurRepository;

    public ChatRESTController(ChatService chatService,
                              com.ditix.backend.Auth.Services.AuthService authService,
                              com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService profilUtilisateurCourantService,
                              com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository profilUtilisateurRepository) {
        this.chatService = chatService;
        this.authService = authService;
        this.profilUtilisateurCourantService = profilUtilisateurCourantService;
        this.profilUtilisateurRepository = profilUtilisateurRepository;
    }

    @GetMapping("/users/search")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DAGE', 'GESTIONNAIRE')")
    public ResponseEntity<List<com.ditix.backend.Chat.DTO.ChatUserSearchDTO>> searchUsers(
            @RequestParam("q") String query,
            JwtAuthenticationToken authentication
    ) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }

        String queryParam = query.trim();
        java.util.UUID currentUserId = profilUtilisateurCourantService.obtenirProfilCourant(authentication).getKeycloakId();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        List<com.ditix.backend.Chat.DTO.ChatUserSearchDTO> results = profilUtilisateurRepository
                .searchActiveUsersForChat(currentUserId, queryParam, pageable)
                .stream()
                .map(p -> new com.ditix.backend.Chat.DTO.ChatUserSearchDTO(
                        p.getKeycloakId().toString(),
                        p.getPrenom(),
                        p.getNom(),
                        p.getRole() != null ? p.getRole().name() : null,
                        p.getStructure() != null ? p.getStructure().getName() : null,
                        (p.getMinistere() != null) ? p.getMinistere().getNom() :
                        (p.getStructure() != null && p.getStructure().getMinistereV2() != null ? p.getStructure().getMinistereV2().getNom() : null)
                ))
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(results);
    }

    // Créer ou récupérer une conversation avec un autre user
    @PostMapping("/conversations")
    public ResponseEntity<Conversation> getOrCreateConversation(
            @RequestBody Map<String, String> body,
            JwtAuthenticationToken authentication
    ) {
        String userId = profilUtilisateurCourantService.obtenirProfilCourant(authentication).getKeycloakId().toString();
        String otherUserId = body.get("otherUserId");
        return ResponseEntity.ok(chatService.getOrCreateConversation(userId, otherUserId));
    }

    // Lister mes conversations
    @GetMapping("/conversations")
    public ResponseEntity<List<Conversation>> getMyConversations(
            JwtAuthenticationToken authentication
    ) {
        String userId = profilUtilisateurCourantService.obtenirProfilCourant(authentication).getKeycloakId().toString();
        return ResponseEntity.ok(chatService.getMyConversations(userId));
    }

    // Historique des messages d'une conversation
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @PathVariable Long conversationId,
            JwtAuthenticationToken authentication
    ) {
        String userId = profilUtilisateurCourantService.obtenirProfilCourant(authentication).getKeycloakId().toString();
        return ResponseEntity.ok(chatService.getMessages(conversationId, userId));
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Map<String, String>> deleteConversation(
            @PathVariable Long id,
            JwtAuthenticationToken authentication
    ) {
        String userId = profilUtilisateurCourantService.obtenirProfilCourant(authentication).getKeycloakId().toString();
        chatService.deleteConversation(id, userId);
        return ResponseEntity.ok(Map.of("message", "Conversation supprimée"));
    }

    @GetMapping("/conversations/{conversationId}/counterpart")
    public ResponseEntity<com.ditix.backend.Chat.DTO.ConversationCounterpartDTO> getCounterpart(
            @PathVariable Long conversationId,
            JwtAuthenticationToken authentication
    ) {
        String requesterUserId = profilUtilisateurCourantService.obtenirProfilCourant(authentication).getKeycloakId().toString();
        String counterpartId = chatService.getCounterpartUserId(conversationId, requesterUserId);

        try {
            Map<String, Object> userMap = authService.getUserById(counterpartId);
            String fullName = (String) userMap.get("fullName");
            if (fullName == null || fullName.trim().isEmpty()) {
                fullName = "Utilisateur indisponible";
            }
            return ResponseEntity.ok(new com.ditix.backend.Chat.DTO.ConversationCounterpartDTO(counterpartId, fullName));
        } catch (Exception e) {
            if (e.getClass().getName().contains("NotFoundException")) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Utilisateur supprimé");
            }
            throw e;
        }
    }
}
