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

    List<ConversationMember> findByConversationIdAndActiveTrue(Long conversationId);

    List<ConversationMember> findByUserIdAndActiveTrue(String userId);

    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO public.conversation_members (conversation_id, user_id, role, active, joined_at) VALUES (:conversationId, :userId, 'MEMBER', TRUE, CURRENT_TIMESTAMP) ON CONFLICT DO NOTHING")
    int insertActiveMemberIfAbsent(@Param("conversationId") Long conversationId, @Param("userId") String userId);
}
