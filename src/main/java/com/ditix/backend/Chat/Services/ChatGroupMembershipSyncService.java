package com.ditix.backend.Chat.Services;

import com.ditix.backend.Chat.DTO.ChatGroupSyncReport;
import com.ditix.backend.Chat.DTO.ChatGroupUser;
import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatGroupMembershipSyncService {

    private final ChatGroupUserDirectory userDirectory;
    private final ChatGroupMembershipApplyService applyService;
    private final ConversationRepository conversationRepository;

    public ChatGroupMembershipSyncService(ChatGroupUserDirectory userDirectory,
                                          ChatGroupMembershipApplyService applyService,
                                          ConversationRepository conversationRepository) {
        this.userDirectory = userDirectory;
        this.applyService = applyService;
        this.conversationRepository = conversationRepository;
    }

    public ChatGroupSyncReport syncGroup(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!conversation.isActive() || !conversation.isSystemManaged() || conversation.getType() == ConversationType.DIRECT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation is not eligible for sync");
        }

        List<String> desiredUserIds = computeDesiredUsers(conversation);

        return applyService.applyGroupSync(conversationId, desiredUserIds);
    }

    public List<ChatGroupSyncReport> syncAllActiveManagedGroups() {
        List<Conversation> activeManagedGroups = conversationRepository.findAll().stream()
                .filter(Conversation::isSystemManaged)
                .filter(Conversation::isActive)
                .filter(c -> c.getType() != ConversationType.DIRECT)
                .toList();

        Map<Long, List<String>> groupDesiredUsers = new HashMap<>();

        for (Conversation group : activeManagedGroups) {
            List<String> desired = computeDesiredUsers(group);
            groupDesiredUsers.put(group.getId(), desired);
        }

        return applyService.applyAllGroupsSync(groupDesiredUsers);
    }

    private List<String> computeDesiredUsers(Conversation conversation) {
        if (conversation.getType() == ConversationType.GLOBAL) {
            return userDirectory.fetchGlobalMembers();
        }

        String ref = conversation.getReferenceId();
        if (ref == null || ref.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group reference ID cannot be blank");
        }
        String cleanRef = ref.trim();

        if (conversation.getType() == ConversationType.COHORT) {
            return userDirectory.fetchCohortMembers(cleanRef);
        } else if (conversation.getType() == ConversationType.STRUCTURE) {
            return userDirectory.fetchStructureMembers(cleanRef);
        } else if (conversation.getType() == ConversationType.MINISTERE) {
            return userDirectory.fetchMinistereMembers(cleanRef);
        }

        return List.of();
    }
}
