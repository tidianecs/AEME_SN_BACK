package com.ditix.backend.Chat.Services;

import com.ditix.backend.Chat.DTO.MessageDTO;
import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.Message;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Chat.Repository.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ChatService(ConversationRepository conversationRepository,
                       MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    // Crée ou récupère une conversation entre deux users
    public Conversation getOrCreateConversation(String userOneId, String userTwoId) {
        return conversationRepository
            .findBetweenUsers(userOneId, userTwoId)
            .orElseGet(() -> {
                Conversation conv = new Conversation();
                conv.setUserOneId(userOneId);
                conv.setUserTwoId(userTwoId);
                return conversationRepository.save(conv);
            });
    }

    // Récupère toutes les conversations d'un user
    public List<Conversation> getMyConversations(String userId) {
        return conversationRepository.findAllByUserId(userId);
    }

    // Récupère l'historique des messages d'une conversation
    public List<MessageDTO> getMessages(Long conversationId, String userId) {
        Conversation conv = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Conversation introuvable"));

        // Vérifie que le user fait partie de la conversation
        if (!conv.getUserOneId().equals(userId) && !conv.getUserTwoId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        return messageRepository
            .findByConversationIdOrderBySentAtAsc(conversationId)
            .stream()
            .map(MessageDTO::new)
            .collect(Collectors.toList());
    }

    // Persiste un message en base
    public MessageDTO saveMessage(Long conversationId, String senderId, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(content);
        return new MessageDTO(messageRepository.save(message));
    }
}
