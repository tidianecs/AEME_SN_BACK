package com.ditix.backend.Chat.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_one_id", length = 255)
    private String userOneId;

    @Column(name = "user_two_id", length = 255)
    private String userTwoId;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ConversationType type = ConversationType.DIRECT;

    @Column(length = 255)
    private String name;

    @Column(name = "reference_id", length = 255)
    private String referenceId;

    @Column(name = "created_by_user_id", length = 255)
    private String createdByUserId;

    @Column(name = "system_managed", nullable = false)
    private boolean systemManaged = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getUserOneId() { 
        return userOneId; 
    }
    public void setUserOneId(String userOneId) { 
        this.userOneId = userOneId; 
    }

    public String getUserTwoId() { 
        return userTwoId; 
    }
    public void setUserTwoId(String userTwoId) { 
        this.userTwoId = userTwoId; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }

    public ConversationType getType() {
        return type;
    }
    public void setType(ConversationType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getReferenceId() {
        return referenceId;
    }
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }
    public void setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public boolean isSystemManaged() {
        return systemManaged;
    }
    public void setSystemManaged(boolean systemManaged) {
        this.systemManaged = systemManaged;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
