package com.ditix.backend.Meeting.DTO;

import java.time.LocalDateTime;
import java.util.List;

public class CreateMeetingRequest {

    private LocalDateTime scheduledAt;
    private List<String> participantIds;

    public LocalDateTime getScheduledAt() { 
        return scheduledAt; 
    }
    public void setScheduledAt(LocalDateTime scheduledAt) { 
        this.scheduledAt = scheduledAt; 
    }

    public List<String> getParticipantIds() { 
        return participantIds; 
    }
    public void setParticipantIds(List<String> participantIds) { 
        this.participantIds = participantIds; 
    }
}
