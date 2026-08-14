package com.ditix.backend.Meeting;

import com.ditix.backend.Meeting.DTO.CreateMeetingRequest;
import com.ditix.backend.Meeting.Repository.MeetingRepository;
import com.ditix.backend.Meeting.Services.MeetingService;
import com.ditix.backend.Notification.Repository.NotificationRepository;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class MeetingTransactionRollbackIntegrationTest {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    // We mock NotificationRepository to simulate a database failure during saveAll
    @MockBean
    private NotificationRepository notificationRepository;

    @Test
    public void testDirectMeetingRollsBackOnNotificationFailure() {
        ProfilUtilisateur creator = new ProfilUtilisateur();
        creator.setKeycloakId(UUID.randomUUID());
        creator.setPrenom("Creator");
        creator.setNom("Rollback");
        creator.setEmail("rollback_creator@test.com" + UUID.randomUUID());
        creator.setRole(RoleUtilisateur.ADMIN);
        creator.setActif(true);
        profilUtilisateurRepository.saveAndFlush(creator);

        ProfilUtilisateur participant = new ProfilUtilisateur();
        participant.setKeycloakId(UUID.randomUUID());
        participant.setPrenom("Participant");
        participant.setNom("Rollback");
        participant.setEmail("rollback_part@test.com" + UUID.randomUUID());
        participant.setRole(RoleUtilisateur.ADMIN);
        participant.setActif(true);
        profilUtilisateurRepository.saveAndFlush(participant);

        CreateMeetingRequest req = new CreateMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        req.setParticipantIds(List.of(participant.getKeycloakId().toString()));

        // Simulate a persistence failure in notifications
        when(notificationRepository.saveAll(any())).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Simulated exception"));

        long meetingsBefore = meetingRepository.count();

        // Execution should throw exception due to rollback
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            meetingService.createMeeting(req, creator.getKeycloakId().toString());
        });

        long meetingsAfter = meetingRepository.count();

        // Verify rollback: no meeting was actually saved to DB
        assertEquals(meetingsBefore, meetingsAfter, "Meeting should not be persisted if notification fails");
    }
}
