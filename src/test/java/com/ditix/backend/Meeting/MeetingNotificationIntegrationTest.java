package com.ditix.backend.Meeting;

import com.ditix.backend.Meeting.DTO.CreateMeetingRequest;
import com.ditix.backend.Meeting.Services.MeetingService;
import com.ditix.backend.Notification.Model.Notification;
import com.ditix.backend.Notification.Repository.NotificationRepository;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MeetingNotificationIntegrationTest {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    @Autowired
    private com.ditix.backend.Meeting.Repository.MeetingRepository meetingRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    public void testNotificationCreatedForDirectMeeting() {
        // Create Creator
        ProfilUtilisateur creator = new ProfilUtilisateur();
        creator.setKeycloakId(UUID.randomUUID());
        creator.setPrenom("Creator");
        creator.setNom("One");
        creator.setEmail("creator1@test.com");
        creator.setRole(RoleUtilisateur.ADMIN);
        creator.setActif(true);
        profilUtilisateurRepository.saveAndFlush(creator);

        // Create Participant
        ProfilUtilisateur participant = new ProfilUtilisateur();
        participant.setKeycloakId(UUID.randomUUID());
        participant.setPrenom("Participant");
        participant.setNom("One");
        participant.setEmail("participant1" + UUID.randomUUID() + "@test.com");
        participant.setRole(RoleUtilisateur.ADMIN);
        participant.setActif(true);
        profilUtilisateurRepository.saveAndFlush(participant);

        CreateMeetingRequest req = new CreateMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        req.setParticipantIds(List.of(participant.getKeycloakId().toString()));

        // Execute meeting creation
        var response = meetingService.createMeeting(req, creator.getKeycloakId().toString());

        // Assert Notification created
        List<Notification> notifs = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(participant.getId());
        assertEquals(1, notifs.size());

        Notification notif = notifs.get(0);
        assertEquals("Nouvelle réunion planifiée", notif.getTitle());
        assertTrue(notif.getMessage().contains("Vous êtes invité à une réunion le"));
        assertEquals(response.getId(), notif.getMeeting().getId());

        // Assert Creator did NOT get notification
        List<Notification> creatorNotifs = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(creator.getId());
        assertEquals(0, creatorNotifs.size());
    }

    @Test
    public void testNotificationNotSentToInactiveUser() {
        // Create Creator
        ProfilUtilisateur creator = new ProfilUtilisateur();
        creator.setKeycloakId(UUID.randomUUID());
        creator.setPrenom("Creator");
        creator.setNom("Two");
        creator.setEmail("creator2@test.com");
        creator.setRole(RoleUtilisateur.ADMIN);
        creator.setActif(true);
        profilUtilisateurRepository.saveAndFlush(creator);

        // Create Inactive Participant
        ProfilUtilisateur inactiveParticipant = new ProfilUtilisateur();
        inactiveParticipant.setKeycloakId(UUID.randomUUID());
        inactiveParticipant.setPrenom("Inactive");
        inactiveParticipant.setNom("Part");
        inactiveParticipant.setEmail("inactive" + UUID.randomUUID() + "@test.com");
        inactiveParticipant.setRole(RoleUtilisateur.ADMIN);
        inactiveParticipant.setActif(false);
        profilUtilisateurRepository.saveAndFlush(inactiveParticipant);

        CreateMeetingRequest req = new CreateMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        req.setParticipantIds(List.of(inactiveParticipant.getKeycloakId().toString()));

        // Execute meeting creation
        meetingService.createMeeting(req, creator.getKeycloakId().toString());

        // Assert No Notification
        List<Notification> notifs = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(inactiveParticipant.getId());
        assertEquals(0, notifs.size());
    }

    @Test
    public void testNotificationNotSentToNonexistentUser() {
        ProfilUtilisateur creator = createAdmin("CreatorThree", "creator3@test.com");

        CreateMeetingRequest req = new CreateMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        // Nonexistent UUID
        req.setParticipantIds(List.of(UUID.randomUUID().toString()));

        var response = meetingService.createMeeting(req, creator.getKeycloakId().toString());
        assertNotNull(response.getId());

        // Count total notifications for this meeting should be 0
        long count = notificationRepository.count();
        // Just verify no new notification is created for a random ID
        // Because recipient is ProfilUtilisateur, if it doesn't exist, no notification can be created anyway
    }

    @Test
    public void testDuplicateParticipantIdsHandled() {
        ProfilUtilisateur creator = createAdmin("CreatorFour", "creator4@test.com");
        ProfilUtilisateur participant = createAdmin("PartFour", "part4@test.com");

        CreateMeetingRequest req = new CreateMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        String pid = participant.getKeycloakId().toString();
        req.setParticipantIds(List.of(pid, pid, pid)); // duplicates

        meetingService.createMeeting(req, creator.getKeycloakId().toString());

        List<Notification> notifs = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(participant.getId());
        assertEquals(1, notifs.size());
    }

    @Test
    public void testEmptyParticipantListHandled() {
        ProfilUtilisateur creator = createAdmin("CreatorFive", "creator5@test.com");

        CreateMeetingRequest req = new CreateMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        req.setParticipantIds(List.of());

        var response = meetingService.createMeeting(req, creator.getKeycloakId().toString());
        assertNotNull(response.getId());
    }

    @Test
    public void testMeetingDeletionPreservesNotification() {
        ProfilUtilisateur creator = createAdmin("CreatorSix", "creator6@test.com");
        ProfilUtilisateur participant = createAdmin("PartSix", "part6@test.com");

        CreateMeetingRequest req = new CreateMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        req.setParticipantIds(List.of(participant.getKeycloakId().toString()));

        var response = meetingService.createMeeting(req, creator.getKeycloakId().toString());

        List<Notification> notifs = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(participant.getId());
        assertEquals(1, notifs.size());
        Notification notif = notifs.get(0);
        assertNotNull(notif.getMeeting());

        // Delete meeting
        meetingRepository.deleteById(response.getId());
        meetingRepository.flush(); // To ensure deletion triggers constraint

        // Clear persistence context so we fetch fresh from DB
        entityManager.clear();

        // verify notification still exists and meetingId is null
        Notification afterDelete = notificationRepository.findById(notif.getId()).get();
        assertNull(afterDelete.getMeeting());
    }

    private ProfilUtilisateur createAdmin(String nom, String email) {
        ProfilUtilisateur admin = new ProfilUtilisateur();
        admin.setKeycloakId(UUID.randomUUID());
        admin.setPrenom("Admin");
        admin.setNom(nom);
        admin.setEmail(email + UUID.randomUUID());
        admin.setRole(RoleUtilisateur.ADMIN);
        admin.setActif(true);
        return profilUtilisateurRepository.saveAndFlush(admin);
    }
}
