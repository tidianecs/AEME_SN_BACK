package com.ditix.backend.Meeting.DTO;

import java.time.LocalDateTime;
import java.util.List;
import com.ditix.backend.Meeting.Model.Meeting;
import com.ditix.backend.Meeting.Model.MeetingType;

public class MeetingResponseDTO {

    private Long id;
    private String roomId;
    private String jitsiUrl;
    private LocalDateTime scheduledAt;
    private String status;
    private List<String> participantIds;
    private String createdByUserId;
    private LocalDateTime createdAt;
    private MeetingType type;
    private Long referenceId;

    public MeetingResponseDTO(Meeting meeting) {
        this.id = meeting.getId();
        this.roomId = meeting.getRoomId();
        this.jitsiUrl = "https://meet.jit.si/" + meeting.getRoomId();
        this.scheduledAt = meeting.getScheduledAt();
        this.status = meeting.getStatus().name();
        this.participantIds = meeting.getParticipantIds();
        this.createdByUserId = meeting.getCreatedByUserId();
        this.createdAt = meeting.getCreatedAt();
        this.type = meeting.getType();
        this.referenceId = meeting.getReferenceId();
    }

    public Long getId() { 
        return id; 
    }
    public String getRoomId() { 
        return roomId; 
    }
    public String getJitsiUrl() { 
        return jitsiUrl; 
    }
    public LocalDateTime getScheduledAt() { 
        return scheduledAt; 
    }
    public String getStatus() { 
        return status; 
    }
    public List<String> getParticipantIds() { 
        return participantIds; 
    }
    public String getCreatedByUserId() { 
        return createdByUserId; 
    }
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    public MeetingType getType() {
        return type;
    }
    public Long getReferenceId() {
        return referenceId;
    }
}