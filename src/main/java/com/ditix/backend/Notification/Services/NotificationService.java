package com.ditix.backend.Notification.Services;

import com.ditix.backend.Meeting.Model.Meeting;
import com.ditix.backend.Meeting.Model.MeetingType;
import com.ditix.backend.Notification.DTO.NotificationDTO;
import com.ditix.backend.Notification.Model.Notification;
import com.ditix.backend.Notification.Model.NotificationType;
import com.ditix.backend.Notification.Repository.NotificationRepository;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProfilUtilisateurRepository profilUtilisateurRepository;

    public NotificationService(NotificationRepository notificationRepository, ProfilUtilisateurRepository profilUtilisateurRepository) {
        this.notificationRepository = notificationRepository;
        this.profilUtilisateurRepository = profilUtilisateurRepository;
    }

    public List<NotificationDTO> getMyNotifications(ProfilUtilisateur profil) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(profil.getId())
                .stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(ProfilUtilisateur profil) {
        return notificationRepository.countUnreadByRecipientId(profil.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId, ProfilUtilisateur profil) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification introuvable"));

        if (!notification.getRecipient().getId().equals(profil.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(ProfilUtilisateur profil) {
        notificationRepository.markAllAsReadByRecipientId(profil.getId());
    }

    @Transactional
    public void createMeetingNotifications(Meeting meeting, List<UUID> targetKeycloakIds) {
        if (targetKeycloakIds == null || targetKeycloakIds.isEmpty()) {
            return;
        }

        List<ProfilUtilisateur> recipients = profilUtilisateurRepository.findActiveProfilesByKeycloakIds(targetKeycloakIds);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String dateStr = meeting.getScheduledAt().format(dateFormatter);
        String timeStr = meeting.getScheduledAt().format(timeFormatter);

        String message = getMessageForMeetingType(meeting.getType(), dateStr, timeStr);

        List<Notification> notifications = new ArrayList<>();
        UUID creatorId = UUID.fromString(meeting.getCreatedByUserId());

        for (ProfilUtilisateur recipient : recipients) {
            // Do NOT notify the creator
            if (recipient.getKeycloakId().equals(creatorId)) {
                continue;
            }

            Notification notif = new Notification();
            notif.setRecipient(recipient);
            notif.setType(NotificationType.MEETING_CREATED);
            notif.setTitle("Nouvelle réunion planifiée");
            notif.setMessage(message);
            notif.setMeeting(meeting);
            notifications.add(notif);
        }

        notificationRepository.saveAll(notifications);
    }

    private String getMessageForMeetingType(MeetingType type, String date, String time) {
        return switch (type) {
            case DIRECT -> "Vous êtes invité à une réunion le " + date + " à " + time + ".";
            case GLOBAL -> "Une réunion générale AEME est prévue le " + date + " à " + time + ".";
            case MINISTERE -> "Une réunion de votre ministère est prévue le " + date + " à " + time + ".";
            case STRUCTURE -> "Une réunion de votre structure est prévue le " + date + " à " + time + ".";
            case COHORT -> "Une réunion de votre cohorte est prévue le " + date + " à " + time + ".";
        };
    }
}
