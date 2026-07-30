package com.ditix.backend.Chat.DTO;

import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationType;

import java.time.LocalDateTime;

public class ManagedChatGroupDTO {
    private Long id;
    private ConversationType type;
    private String name;
    private String referenceId;
    private boolean systemManaged;
    private boolean active;
    private String createdByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ManagedChatGroupDTO(Conversation conversation) {
        this.id = conversation.getId();
        this.type = conversation.getType();
        this.name = conversation.getName();
        this.referenceId = conversation.getReferenceId();
        this.systemManaged = conversation.isSystemManaged();
        this.active = conversation.isActive();
        this.createdByUserId = conversation.getCreatedByUserId();
        this.createdAt = conversation.getCreatedAt();
        this.updatedAt = conversation.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ConversationType getType() { return type; }
    public void setType(ConversationType type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public boolean isSystemManaged() { return systemManaged; }
    public void setSystemManaged(boolean systemManaged) { this.systemManaged = systemManaged; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(String createdByUserId) { this.createdByUserId = createdByUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
