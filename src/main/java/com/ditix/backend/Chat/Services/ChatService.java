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
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ChatService(ConversationRepository conversationRepository,
                       MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    public boolean canAccessConversation(Long conversationId, String userId) {
        if (conversationId == null || userId == null || userId.trim().isEmpty()) {
            return false;
        }
        return conversationRepository.findById(conversationId)
                .map(conv -> java.util.Objects.equals(userId, conv.getUserOneId()) ||
                             java.util.Objects.equals(userId, conv.getUserTwoId()))
                .orElse(false);
    }

    public String getCounterpartUserId(Long conversationId, String requesterUserId) {
        if (conversationId == null || requesterUserId == null || requesterUserId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"));

        if (java.util.Objects.equals(requesterUserId, conv.getUserOneId())) {
            String counterpart = conv.getUserTwoId();
            if (counterpart == null || counterpart.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
            }
            return counterpart;
        } else if (java.util.Objects.equals(requesterUserId, conv.getUserTwoId())) {
            String counterpart = conv.getUserOneId();
            if (counterpart == null || counterpart.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
            }
            return counterpart;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }
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

    @Transactional
    public void deleteConversation(Long id, String userId) {
        Conversation conv = conversationRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Conversation introuvable"));

        if (!conv.getUserOneId().equals(userId) && !conv.getUserTwoId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        // Supprime d'abord les messages de la conversation
        messageRepository.deleteByConversationId(id);
        conversationRepository.delete(conv);
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
