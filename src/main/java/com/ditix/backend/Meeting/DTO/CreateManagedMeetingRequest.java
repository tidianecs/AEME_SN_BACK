package com.ditix.backend.Meeting.DTO;

import java.time.LocalDateTime;

public class CreateManagedMeetingRequest {
    private LocalDateTime scheduledAt;
    private Long targetId;

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
}
