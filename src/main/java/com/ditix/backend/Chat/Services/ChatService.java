package com.ditix.backend.Chat.Services;

import com.ditix.backend.Chat.DTO.MessageDTO;
import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.Message;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Chat.Repository.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationMemberRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    public ChatService(ConversationRepository conversationRepository,
                       MessageRepository messageRepository,
                       ConversationMemberRepository conversationMemberRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.conversationMemberRepository = conversationMemberRepository;
    }

    public boolean canAccessConversation(Long conversationId, String userId) {
        if (conversationId == null || userId == null || userId.trim().isEmpty()) {
            return false;
        }
        return conversationRepository.findById(conversationId)
                .map(conv -> conv.isActive() && conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(conversationId, userId))
                .orElse(false);
    }

    public Conversation requireActiveConversationMember(Long conversationId, String userId) {
        if (conversationId == null || userId == null || userId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }
        Conversation conv = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable"));

        if (!conv.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        if (!conversationMemberRepository.existsByConversationIdAndUserIdAndActiveTrue(conversationId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }
        return conv;
    }

    public String getCounterpartUserId(Long conversationId, String requesterUserId) {
        if (conversationId == null || requesterUserId == null || requesterUserId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        Conversation conv = requireActiveConversationMember(conversationId, requesterUserId);

        if (conv.getType() != ConversationType.DIRECT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Counterpart not applicable for this conversation type");
        }

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
    @Transactional
    public Conversation getOrCreateConversation(String userOneId, String userTwoId) {
        Conversation conversation = conversationRepository
            .findBetweenUsers(userOneId, userTwoId)
            .orElseGet(() -> {
                Conversation conv = new Conversation();
                conv.setUserOneId(userOneId);
                conv.setUserTwoId(userTwoId);
                return conversationRepository.save(conv);
            });

        ensureDirectMembers(conversation);
        return conversation;
    }

    private void ensureDirectMembers(Conversation conversation) {
        if (conversation == null || conversation.getId() == null) {
            return;
        }
        if (conversation.getType() != ConversationType.DIRECT) {
            return;
        }

        String u1 = conversation.getUserOneId();
        String u2 = conversation.getUserTwoId();

        if (u1 != null && !u1.trim().isEmpty()) {
            conversationMemberRepository.insertActiveMemberIfAbsent(conversation.getId(), u1);
        }

        if (u2 != null && !u2.trim().isEmpty() && !u2.equals(u1)) {
            conversationMemberRepository.insertActiveMemberIfAbsent(conversation.getId(), u2);
        }
    }

    // Récupère toutes les conversations d'un user
    public List<Conversation> getMyConversations(String userId) {
        return conversationRepository.findAllByUserId(userId);
    }

    // Récupère l'historique des messages d'une conversation
    public List<MessageDTO> getMessages(Long conversationId, String userId) {
        Conversation conv = requireActiveConversationMember(conversationId, userId);

        return messageRepository
            .findByConversationIdOrderBySentAtAsc(conversationId)
            .stream()
            .map(MessageDTO::new)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteConversation(Long id, String userId) {
        Conversation conv = requireActiveConversationMember(id, userId);

        if (conv.getType() != ConversationType.DIRECT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seule une conversation directe peut être supprimée");
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
