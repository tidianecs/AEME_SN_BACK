package com.ditix.backend.Chat.Services;

import com.ditix.backend.Chat.DTO.ChatGroupSyncReport;
import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationMember;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationMemberRepository;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ChatGroupMembershipApplyService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final EntityManager entityManager;

    public ChatGroupMembershipApplyService(ConversationRepository conversationRepository,
                                           ConversationMemberRepository conversationMemberRepository,
                                           EntityManager entityManager) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.entityManager = entityManager;
    }

    private void acquireAdvisoryLock(Long conversationId) {
        String lockKey = "chat-group-membership-sync:" + conversationId;
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(hashtextextended(:lockKey, 0))")
                .setParameter("lockKey", lockKey)
                .getSingleResult();
    }

    @Transactional
    public ChatGroupSyncReport applyGroupSync(Long conversationId, List<String> desiredUserIds) {
        return applySingleGroup(conversationId, desiredUserIds);
    }

    @Transactional
    public List<ChatGroupSyncReport> applyAllGroupsSync(Map<Long, List<String>> groupDesiredUsers) {
        List<ChatGroupSyncReport> reports = new ArrayList<>();

        List<Long> sortedIds = new ArrayList<>(groupDesiredUsers.keySet());
        sortedIds.sort(Long::compareTo);

        for (Long conversationId : sortedIds) {
            ChatGroupSyncReport report = applySingleGroup(conversationId, groupDesiredUsers.get(conversationId));
            reports.add(report);
        }
        return reports;
    }

    private ChatGroupSyncReport applySingleGroup(Long conversationId, List<String> desiredUserIds) {
        acquireAdvisoryLock(conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!conversation.isActive() || !conversation.isSystemManaged() || conversation.getType() == ConversationType.DIRECT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation is not eligible for sync");
        }

        List<ConversationMember> activeMembers = conversationMemberRepository.findByConversationIdAndActiveTrue(conversationId);
        int activeBefore = activeMembers.size();

        int deactivated = 0;
        if (desiredUserIds == null || desiredUserIds.isEmpty()) {
            deactivated = conversationMemberRepository.deactivateAllMembers(conversationId);
        } else {
            deactivated = conversationMemberRepository.deactivateMembersNotInList(conversationId, desiredUserIds);
        }

        int added = 0;
        int unchanged = 0;

        if (desiredUserIds != null && !desiredUserIds.isEmpty()) {
            List<String> currentActiveUserIds = conversationMemberRepository.findByConversationIdAndActiveTrue(conversationId)
                    .stream().map(ConversationMember::getUserId).toList();

            for (String userId : desiredUserIds) {
                if (currentActiveUserIds.contains(userId)) {
                    unchanged++;
                } else {
                    int rows = conversationMemberRepository.insertSyncMemberIfAbsent(conversationId, userId);
                    added += rows;
                }
            }
        }

        int activeAfter = conversationMemberRepository.findByConversationIdAndActiveTrue(conversationId).size();

        return new ChatGroupSyncReport(
                conversation.getId(),
                conversation.getType(),
                conversation.getReferenceId(),
                desiredUserIds == null ? 0 : desiredUserIds.size(),
                activeBefore,
                added,
                deactivated,
                unchanged,
                activeAfter,
                LocalDateTime.now()
        );
    }
}
