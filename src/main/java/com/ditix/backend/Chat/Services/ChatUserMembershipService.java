package com.ditix.backend.Chat.Services;

import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationMember;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationMemberRepository;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ChatUserMembershipService {

    private static final Logger logger = LoggerFactory.getLogger(ChatUserMembershipService.class);

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    public ChatUserMembershipService(ConversationRepository conversationRepository,
                                     ConversationMemberRepository conversationMemberRepository) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
    }

    @Transactional
    public void syncUserMemberships(ProfilUtilisateur profil) {
        if (profil == null || profil.getKeycloakId() == null) {
            return;
        }

        String userId = profil.getKeycloakId().toString();
        Set<Long> expectedConversationIds = new HashSet<>();

        // 1. Identify expected groups
        conversationRepository.findByTypeAndActiveTrue(ConversationType.GLOBAL)
                .ifPresent(c -> expectedConversationIds.add(c.getId()));

        if (profil.getRole() == RoleUtilisateur.DAGE) {
            if (profil.getMinistere() != null) {
                conversationRepository.findByTypeAndReferenceIdAndActiveTrue(
                        ConversationType.MINISTERE, profil.getMinistere().getId().toString())
                        .ifPresent(c -> expectedConversationIds.add(c.getId()));
            }
        } else if (profil.getRole() == RoleUtilisateur.GESTIONNAIRE) {
            if (profil.getCohorte() != null) {
                conversationRepository.findByTypeAndReferenceIdAndActiveTrue(
                        ConversationType.COHORT, profil.getCohorte().getId().toString())
                        .ifPresent(c -> expectedConversationIds.add(c.getId()));
            }
            if (profil.getStructure() != null) {
                conversationRepository.findByTypeAndReferenceIdAndActiveTrue(
                        ConversationType.STRUCTURE, profil.getStructure().getId().toString())
                        .ifPresent(c -> expectedConversationIds.add(c.getId()));

                if (profil.getStructure().getMinistereV2() != null) {
                    conversationRepository.findByTypeAndReferenceIdAndActiveTrue(
                            ConversationType.MINISTERE, profil.getStructure().getMinistereV2().getId().toString())
                            .ifPresent(c -> expectedConversationIds.add(c.getId()));
                }
            }
        }

        // 2. Fetch current active memberships
        List<ConversationMember> currentActiveMemberships = conversationMemberRepository.findByUserIdAndActiveTrue(userId);
        List<Long> currentActiveConversationIds = currentActiveMemberships.stream()
                .map(ConversationMember::getConversationId)
                .toList();

        // 3. Deactivate obsolete managed-group memberships
        if (!currentActiveConversationIds.isEmpty()) {
            List<Conversation> currentConversations = conversationRepository.findAllById(currentActiveConversationIds);
            for (Conversation conv : currentConversations) {
                if (conv.isSystemManaged() && conv.getType() != ConversationType.DIRECT) {
                    if (!expectedConversationIds.contains(conv.getId())) {
                        conversationMemberRepository.deactivateSpecificMember(conv.getId(), userId);
                        logger.info("Deactivated obsolete membership for user {} in conversation {}", userId, conv.getId());
                    }
                }
            }
        }

        // 4. Add missing active memberships (or reactivate)
        for (Long expectedId : expectedConversationIds) {
            if (!currentActiveConversationIds.contains(expectedId)) {
                java.util.Optional<ConversationMember> existingOpt = conversationMemberRepository.findFirstByConversationIdAndUserIdOrderByIdDesc(expectedId, userId);
                if (existingOpt.isPresent()) {
                    ConversationMember existing = existingOpt.get();
                    if (!existing.isActive()) {
                        existing.setActive(true);
                        existing.setLeftAt(null);
                        existing.setJoinedAt(java.time.LocalDateTime.now());
                        conversationMemberRepository.save(existing);
                        logger.info("Reactivated expected membership for user {} in conversation {}", userId, expectedId);
                    }
                } else {
                    conversationMemberRepository.insertSyncMemberIfAbsent(expectedId, userId);
                    logger.info("Added expected membership for user {} in conversation {}", userId, expectedId);
                }
            }
        }
    }

    @Transactional
    public void deactivateManagedMemberships(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }

        List<ConversationMember> currentActiveMemberships = conversationMemberRepository.findByUserIdAndActiveTrue(userId);
        if (currentActiveMemberships.isEmpty()) {
            return;
        }

        List<Long> currentActiveConversationIds = currentActiveMemberships.stream()
                .map(ConversationMember::getConversationId)
                .toList();

        List<Conversation> currentConversations = conversationRepository.findAllById(currentActiveConversationIds);
        for (Conversation conv : currentConversations) {
            if (conv.isSystemManaged() && conv.getType() != ConversationType.DIRECT) {
                conversationMemberRepository.deactivateSpecificMember(conv.getId(), userId);
                logger.info("Deactivated system-managed membership for user {} in conversation {}", userId, conv.getId());
            }
        }
    }
}
