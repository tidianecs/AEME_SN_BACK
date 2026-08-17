package com.ditix.backend.Notification;

import com.ditix.backend.Meeting.Model.Meeting;
import com.ditix.backend.Meeting.Repository.MeetingRepository;
import com.ditix.backend.Notification.Model.Notification;
import com.ditix.backend.Notification.Model.NotificationType;
import com.ditix.backend.Notification.Repository.NotificationRepository;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private ProfilUtilisateur createUser(String name) {
        ProfilUtilisateur user = new ProfilUtilisateur();
        user.setKeycloakId(UUID.randomUUID());
        user.setPrenom("Test");
        user.setNom(name);
        user.setEmail(name + "@test.com");
        user.setRole(RoleUtilisateur.ADMIN);
        user.setActif(true);
        return profilUtilisateurRepository.saveAndFlush(user);
    }

    private Notification createNotification(ProfilUtilisateur recipient) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle("Title");
        notification.setMessage("Message");
        notification.setType(NotificationType.MEETING_CREATED);
        return notificationRepository.saveAndFlush(notification);
    }

    @Test
    public void testNoJwtReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testOwnListIsolationAndOrdering() throws Exception {
        ProfilUtilisateur userA = createUser("userA");
        userA.setKeycloakId(UUID.fromString("00000000-0000-0000-0000-00000000000a"));
        profilUtilisateurRepository.saveAndFlush(userA);

        ProfilUtilisateur userB = createUser("userB");

        Notification notifB = createNotification(userB);

        Notification notifA1 = createNotification(userA);
        Thread.sleep(100);
        Notification notifA2 = createNotification(userA);

        mockMvc.perform(get("/api/v1/notifications")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(userA.getKeycloakId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(notifA2.getId())) // Newest first
                .andExpect(jsonPath("$[1].id").value(notifA1.getId()));
    }

    @Test
    public void testUnreadCount() throws Exception {
        ProfilUtilisateur userA = createUser("userC");
        userA.setKeycloakId(UUID.fromString("00000000-0000-0000-0000-00000000000c"));
        profilUtilisateurRepository.saveAndFlush(userA);

        createNotification(userA);
        createNotification(userA);

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(userA.getKeycloakId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(2));
    }

    @Test
    public void testOwnMarkRead() throws Exception {
        ProfilUtilisateur userA = createUser("userD");
        userA.setKeycloakId(UUID.fromString("00000000-0000-0000-0000-00000000000d"));
        profilUtilisateurRepository.saveAndFlush(userA);

        Notification notif = createNotification(userA);

        mockMvc.perform(patch("/api/v1/notifications/" + notif.getId() + "/read")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(userA.getKeycloakId().toString()))))
                .andExpect(status().isOk());

        Notification updated = notificationRepository.findById(notif.getId()).get();
        assertNotNull(updated.getReadAt());
    }

    @Test
    public void testCrossUserMarkReadProtected() throws Exception {
        ProfilUtilisateur userA = createUser("userE");
        ProfilUtilisateur userB = createUser("userF");
        userB.setKeycloakId(UUID.fromString("00000000-0000-0000-0000-00000000000f"));
        profilUtilisateurRepository.saveAndFlush(userB);

        Notification notifA = createNotification(userA);

        mockMvc.perform(patch("/api/v1/notifications/" + notifA.getId() + "/read")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(userB.getKeycloakId().toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testReadAllIsolation() throws Exception {
        ProfilUtilisateur userA = createUser("userG");
        userA.setKeycloakId(UUID.fromString("00000000-0000-0000-0000-000000000010"));
        profilUtilisateurRepository.saveAndFlush(userA);

        ProfilUtilisateur userB = createUser("userH");

        Notification notifA = createNotification(userA);
        Notification notifB = createNotification(userB);

        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(userA.getKeycloakId().toString()))))
                .andExpect(status().isOk());

        entityManager.clear();

        assertNotNull(notificationRepository.findById(notifA.getId()).get().getReadAt());
        org.junit.jupiter.api.Assertions.assertNull(notificationRepository.findById(notifB.getId()).get().getReadAt());
    }

    @Test
    public void testNonexistentNotification() throws Exception {
        ProfilUtilisateur userA = createUser("userI");
        userA.setKeycloakId(UUID.fromString("00000000-0000-0000-0000-000000000011"));
        profilUtilisateurRepository.saveAndFlush(userA);

        mockMvc.perform(patch("/api/v1/notifications/999999/read")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(userA.getKeycloakId().toString()))))
                .andExpect(status().isNotFound());
    }
}
