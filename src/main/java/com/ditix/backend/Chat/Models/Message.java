package com.ditix.backend.Chat.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long conversationId;

    @Column(nullable = false)
    private String senderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sender_full_name", length = 255)
    private String senderFullName;

    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        this.sentAt = LocalDateTime.now();
    }

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public Long getConversationId() { 
        return conversationId; 
    }
    public void setConversationId(Long conversationId) { 
        this.conversationId = conversationId; 
    }

    public String getSenderId() { 
        return senderId; 
    }
    public void setSenderId(String senderId) { 
        this.senderId = senderId; 
    }

    public String getContent() { 
        return content; 
    }
    public void setContent(String content) { 
        this.content = content; 
    }

    public LocalDateTime getSentAt() { 
        return sentAt; 
    }

    public String getSenderFullName() {
        return senderFullName;
    }
    public void setSenderFullName(String senderFullName) {
        this.senderFullName = senderFullName;
    }
}
