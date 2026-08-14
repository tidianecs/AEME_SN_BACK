package com.ditix.backend.Notification.DTO;

import com.ditix.backend.Notification.Model.Notification;
import java.time.LocalDateTime;

public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String message;
    private Long meetingId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public NotificationDTO() {}

    public NotificationDTO(Notification notif) {
        this.id = notif.getId();
        this.type = notif.getType().name();
        this.title = notif.getTitle();
        this.message = notif.getMessage();
        this.meetingId = notif.getMeeting() != null ? notif.getMeeting().getId() : null;
        this.createdAt = notif.getCreatedAt();
        this.readAt = notif.getReadAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
