package com.ditix.backend.Chat.Repository;

import com.ditix.backend.Chat.Models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Trouve une conversation entre deux users (dans n'importe quel ordre)
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.userOneId = :userOneId AND c.userTwoId = :userTwoId) OR " +
           "(c.userOneId = :userTwoId AND c.userTwoId = :userOneId)")
    Optional<Conversation> findBetweenUsers(String userOneId, String userTwoId);

    // Toutes les conversations d'un user
    @Query("SELECT c FROM Conversation c JOIN ConversationMember cm ON c.id = cm.conversationId WHERE " +
           "cm.userId = :userId AND cm.active = true AND c.active = true ORDER BY COALESCE(c.updatedAt, c.createdAt) DESC")
    List<Conversation> findAllByUserId(String userId);

    Optional<Conversation> findByTypeAndActiveTrue(com.ditix.backend.Chat.Models.ConversationType type);

    Optional<Conversation> findByTypeAndReferenceIdAndActiveTrue(com.ditix.backend.Chat.Models.ConversationType type, String referenceId);

    @Query("SELECT c FROM Conversation c WHERE c.type <> com.ditix.backend.Chat.Models.ConversationType.DIRECT ORDER BY c.active DESC, c.createdAt DESC, c.id DESC")
    List<Conversation> findByTypeNotOrderByCreatedAtDesc();

    @Query(value = "SELECT pg_advisory_xact_lock(hashtextextended(:lockKey, 0))", nativeQuery = true)
    void acquireAdvisoryXactLock(String lockKey);

    @Query(value = "INSERT INTO public.conversations (" +
                   "created_at, type, name, reference_id, created_by_user_id, system_managed, active, updated_at" +
                   ") VALUES (" +
                   "CURRENT_TIMESTAMP, :type, :name, :referenceId, :createdByUserId, TRUE, TRUE, CURRENT_TIMESTAMP" +
                   ") ON CONFLICT (type) WHERE type = 'GLOBAL' AND active = true DO NOTHING RETURNING id", nativeQuery = true)
    Long insertGlobalGroupAtomically(String type, String name, String referenceId, String createdByUserId);

    @Query(value = "INSERT INTO public.conversations (" +
                   "created_at, type, name, reference_id, created_by_user_id, system_managed, active, updated_at" +
                   ") VALUES (" +
                   "CURRENT_TIMESTAMP, :type, :name, :referenceId, :createdByUserId, TRUE, TRUE, CURRENT_TIMESTAMP" +
                   ") ON CONFLICT (type, reference_id) WHERE active = true AND type IN ('COHORT', 'STRUCTURE', 'MINISTERE') AND reference_id IS NOT NULL DO NOTHING RETURNING id", nativeQuery = true)
    Long insertCohortStructureOrMinistereGroupAtomically(String type, String name, String referenceId, String createdByUserId);
}
