package com.ditix.backend.Meeting.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomId; //FOR JITSI

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "meeting_participants",
        joinColumns = @JoinColumn(name = "meeting_id"))
    @Column(name = "user_id")
    private List<String> participantIds;

    @Column(nullable = false)
    private String createdByUserId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = MeetingStatus.SCHEDULED;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getRoomId() { 
        return roomId; 
    }
    public void setRoomId(String roomId) { 
        this.roomId = roomId; 
    }

    public LocalDateTime getScheduledAt() { 
        return scheduledAt; 
    }
    public void setScheduledAt(LocalDateTime scheduledAt) { 
        this.scheduledAt = scheduledAt; 
    }

    public MeetingStatus getStatus() { 
        return status; 
    }
    public void setStatus(MeetingStatus status) { 
        this.status = status; 
    }

    public List<String> getParticipantIds() { 
        return participantIds; 
    }
    public void setParticipantIds(List<String> participantIds) { 
        this.participantIds = participantIds; 
    }

    public String getCreatedByUserId() { 
        return createdByUserId; 
    }
    public void setCreatedByUserId(String createdByUserId) { 
        this.createdByUserId = createdByUserId; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
}
