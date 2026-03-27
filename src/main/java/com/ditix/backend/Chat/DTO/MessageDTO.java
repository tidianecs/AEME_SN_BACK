package com.ditix.backend.Chat.DTO;

import com.ditix.backend.Chat.Models.Message;
import java.time.LocalDateTime;

public class MessageDTO {

    private Long id;
    private Long conversationId;
    private String senderId;
    private String content;
    private LocalDateTime sentAt;

    public MessageDTO(Message message) {
        this.id = message.getId();
        this.conversationId = message.getConversationId();
        this.senderId = message.getSenderId();
        this.content = message.getContent();
        this.sentAt = message.getSentAt();
    }

    // Constructor pour WebSocket (message pas encore persisté)
    public MessageDTO(Long conversationId, String senderId, String content) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.sentAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getConversationId() { return conversationId; }
    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setId(Long id) { this.id = id; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
