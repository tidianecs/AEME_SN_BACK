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
    @Query("SELECT c FROM Conversation c WHERE " +
           "c.userOneId = :userId OR c.userTwoId = :userId")
    List<Conversation> findAllByUserId(String userId);
}
