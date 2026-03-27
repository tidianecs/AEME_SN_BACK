package com.ditix.backend.Chat.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userOneId;

    @Column(nullable = false)
    private String userTwoId;

    private LocalDateTime createdAt;

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
}
