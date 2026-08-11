package com.ditix.backend.Chat.Repository;

import com.ditix.backend.Chat.Models.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

    boolean existsByConversationIdAndUserIdAndActiveTrue(Long conversationId, String userId);

    Optional<ConversationMember> findByConversationIdAndUserIdAndActiveTrue(Long conversationId, String userId);

    Optional<ConversationMember> findFirstByConversationIdAndUserIdOrderByIdDesc(Long conversationId, String userId);

    List<ConversationMember> findByConversationIdAndActiveTrue(Long conversationId);

    List<ConversationMember> findByUserIdAndActiveTrue(String userId);

    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO public.conversation_members (conversation_id, user_id, role, active, joined_at) VALUES (:conversationId, :userId, 'MEMBER', TRUE, CURRENT_TIMESTAMP) ON CONFLICT DO NOTHING")
    int insertActiveMemberIfAbsent(@Param("conversationId") Long conversationId, @Param("userId") String userId);

    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO public.conversation_members (conversation_id, user_id, role, active, joined_at, left_at) VALUES (:conversationId, :userId, 'MEMBER', TRUE, CURRENT_TIMESTAMP, NULL) ON CONFLICT DO NOTHING")
    int insertSyncMemberIfAbsent(@Param("conversationId") Long conversationId, @Param("userId") String userId);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE public.conversation_members SET active = false, left_at = CURRENT_TIMESTAMP WHERE conversation_id = :conversationId AND active = true AND user_id NOT IN :desiredUserIds")
    int deactivateMembersNotInList(@Param("conversationId") Long conversationId, @Param("desiredUserIds") List<String> desiredUserIds);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE public.conversation_members SET active = false, left_at = CURRENT_TIMESTAMP WHERE conversation_id = :conversationId AND active = true")
    int deactivateAllMembers(@Param("conversationId") Long conversationId);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE public.conversation_members SET active = false, left_at = CURRENT_TIMESTAMP WHERE conversation_id = :conversationId AND user_id = :userId AND active = true")
    int deactivateSpecificMember(@Param("conversationId") Long conversationId, @Param("userId") String userId);
}
