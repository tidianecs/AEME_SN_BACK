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

    public ChatRESTController(ChatService chatService) {
        this.chatService = chatService;
    }

    // Créer ou récupérer une conversation avec un autre user
    @PostMapping("/conversations")
    public ResponseEntity<Conversation> getOrCreateConversation(
            @RequestBody Map<String, String> body,
            JwtAuthenticationToken authentication
    ) {
        String userId = authentication.getToken().getSubject();
        String otherUserId = body.get("otherUserId");
        return ResponseEntity.ok(chatService.getOrCreateConversation(userId, otherUserId));
    }

    // Lister mes conversations
    @GetMapping("/conversations")
    public ResponseEntity<List<Conversation>> getMyConversations(
            JwtAuthenticationToken authentication
    ) {
        String userId = authentication.getToken().getSubject();
        return ResponseEntity.ok(chatService.getMyConversations(userId));
    }

    // Historique des messages d'une conversation
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @PathVariable Long conversationId,
            JwtAuthenticationToken authentication
    ) {
        String userId = authentication.getToken().getSubject();
        return ResponseEntity.ok(chatService.getMessages(conversationId, userId));
    }
}
