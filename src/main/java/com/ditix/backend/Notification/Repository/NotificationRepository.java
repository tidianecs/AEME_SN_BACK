package com.ditix.backend.Notification.Repository;

import com.ditix.backend.Notification.Model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :recipientId AND n.readAt IS NULL")
    long countUnreadByRecipientId(@Param("recipientId") Long recipientId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Notification n SET n.readAt = CURRENT_TIMESTAMP WHERE n.recipient.id = :recipientId AND n.readAt IS NULL")
    int markAllAsReadByRecipientId(@Param("recipientId") Long recipientId);
}
