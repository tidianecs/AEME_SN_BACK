package com.ditix.backend.Chat.Services;

import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ManagedChatGroupService {

    private final ConversationRepository conversationRepository;
    private final com.ditix.backend.Ministere.Repository.MinistereRepository ministereRepository;
    private final ChatGroupMembershipSyncService syncService;

    public ManagedChatGroupService(ConversationRepository conversationRepository,
                                   com.ditix.backend.Ministere.Repository.MinistereRepository ministereRepository,
                                   ChatGroupMembershipSyncService syncService) {
        this.conversationRepository = conversationRepository;
        this.ministereRepository = ministereRepository;
        this.syncService = syncService;
    }

    @Transactional
    public Conversation createOrGetGlobalGroup(String name, String creatorUserId) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
        }
        if (name.trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot exceed 255 characters");
        }

        return conversationRepository.findByTypeAndActiveTrue(ConversationType.GLOBAL)
                .orElseGet(() -> createGroup(ConversationType.GLOBAL, "GLOBAL", name, creatorUserId));
    }

    @Transactional
    public Conversation createOrGetCohortGroup(String cohortReference, String name, String creatorUserId) {
        if (cohortReference == null || cohortReference.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reference ID cannot be blank");
        }
        if (cohortReference.trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reference ID cannot exceed 255 characters");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
        }
        if (name.trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot exceed 255 characters");
        }

        String ref = cohortReference.trim();
        return conversationRepository.findByTypeAndReferenceIdAndActiveTrue(ConversationType.COHORT, ref)
                .orElseGet(() -> createGroup(ConversationType.COHORT, ref, name, creatorUserId));
    }

    @Transactional
    public Conversation createOrGetStructureGroup(String structureId, String name, String creatorUserId) {
        if (structureId == null || structureId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reference ID cannot be blank");
        }
        if (structureId.trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reference ID cannot exceed 255 characters");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
        }
        if (name.trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot exceed 255 characters");
        }

        String ref = structureId.trim();
        return conversationRepository.findByTypeAndReferenceIdAndActiveTrue(ConversationType.STRUCTURE, ref)
                .orElseGet(() -> createGroup(ConversationType.STRUCTURE, ref, name, creatorUserId));
    }

    @Transactional
    public Conversation createOrGetMinistereGroup(String ministereId, String name, String creatorUserId) {
        if (ministereId == null || ministereId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reference ID cannot be blank");
        }
        if (ministereId.trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reference ID cannot exceed 255 characters");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
        }
        if (name.trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot exceed 255 characters");
        }

        String ref = ministereId.trim();
        Long ministereIdLong;
        try {
            ministereIdLong = Long.parseLong(ref);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ministere ID format");
        }

        com.ditix.backend.Ministere.Model.Ministere ministere = ministereRepository.findById(ministereIdLong)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ministere introuvable"));

        if (!Boolean.TRUE.equals(ministere.getActif())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Impossible de créer un groupe pour un ministère inactif");
        }
        return conversationRepository.findByTypeAndReferenceIdAndActiveTrue(ConversationType.MINISTERE, ref)
                .orElseGet(() -> createGroup(ConversationType.MINISTERE, ref, name, creatorUserId));
    }

    private Conversation createGroup(ConversationType type, String referenceId, String name, String creatorUserId) {
        String cleanName = name.trim();
        Long id;
        if (type == ConversationType.GLOBAL) {
            id = conversationRepository.insertGlobalGroupAtomically(type.name(), cleanName, referenceId, creatorUserId);
        } else {
            id = conversationRepository.insertCohortStructureOrMinistereGroupAtomically(type.name(), cleanName, referenceId, creatorUserId);
        }

        if (id == null) {
            // Unflushed concurrent creation, wait for the other transaction and return it
            if (type == ConversationType.GLOBAL) {
                return conversationRepository.findByTypeAndActiveTrue(type)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Global group creation conflict"));
            } else {
                return conversationRepository.findByTypeAndReferenceIdAndActiveTrue(type, referenceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Group creation conflict"));
            }
        }

        syncService.syncGroup(id);

        return conversationRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load created group"));
    }

    public List<Conversation> listManagedGroups() {
        return conversationRepository.findByTypeNotOrderByCreatedAtDesc();
    }

    @Transactional
    public Conversation archiveGroup(Long conversationId, String requesterUserId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable"));

        if (conv.getType() == ConversationType.DIRECT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Les conversations directes ne peuvent pas être archivées via ce service");
        }

        conv.setActive(false);
        conv.setUpdatedAt(LocalDateTime.now());
        return conversationRepository.save(conv);
    }

    @Transactional
    public Conversation reactivateGroup(Long conversationId, String requesterUserId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable"));

        if (conv.getType() == ConversationType.DIRECT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Les conversations directes ne peuvent pas être réactivées via ce service");
        }

        // Check uniqueness constraint before reactivating
        String lockKey;
        if (conv.getType() == ConversationType.GLOBAL) {
            lockKey = "GLOBAL";
        } else {
            lockKey = conv.getType().name() + ":" + conv.getReferenceId();
        }

        conversationRepository.acquireAdvisoryXactLock(lockKey);

        if (conv.getType() == ConversationType.GLOBAL) {
            if (conversationRepository.findByTypeAndActiveTrue(ConversationType.GLOBAL).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Un groupe GLOBAL actif existe déjà");
            }
        } else if (conv.getType() == ConversationType.COHORT || conv.getType() == ConversationType.STRUCTURE || conv.getType() == ConversationType.MINISTERE) {
            if (conversationRepository.findByTypeAndReferenceIdAndActiveTrue(conv.getType(), conv.getReferenceId()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Un groupe actif avec cette référence existe déjà");
            }
        }

        conv.setActive(true);
        conv.setUpdatedAt(LocalDateTime.now());
        return conversationRepository.save(conv);
    }
}
