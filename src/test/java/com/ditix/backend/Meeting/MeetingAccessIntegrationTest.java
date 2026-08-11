package com.ditix.backend.Meeting;

import com.ditix.backend.Meeting.Model.Meeting;
import com.ditix.backend.Meeting.Model.MeetingType;
import com.ditix.backend.Meeting.Repository.MeetingRepository;
import com.ditix.backend.Meeting.Services.MeetingAutorisationService;
import com.ditix.backend.Meeting.Services.MeetingService;
import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import org.junit.jupiter.api.BeforeEach;
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
public class MeetingAccessIntegrationTest {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingAutorisationService autorisationService;

    private ProfilUtilisateur admin;
    private ProfilUtilisateur dageA;
    private ProfilUtilisateur dageB;
    private ProfilUtilisateur gestionnaireA;
    private ProfilUtilisateur gestionnaireB;

    private Ministere minA;
    private Ministere minB;
    private Cohorte cohorteA;
    private Cohorte cohorteB;
    private Structure structA;
    private Structure structB;

    private Meeting globalMeeting;
    private Meeting cohortAMeeting;
    private Meeting cohortBMeeting;
    private Meeting structAMeeting;
    private Meeting structBMeeting;
    private Meeting minAMeeting;
    private Meeting minBMeeting;
    private Meeting directA;
    private Meeting directB;

    @BeforeEach
    public void setup() {
        minA = new Ministere(); minA.setId(10L);
        minB = new Ministere(); minB.setId(20L);

        cohorteA = new Cohorte(); cohorteA.setId(100L);
        cohorteB = new Cohorte(); cohorteB.setId(200L);

        structA = new Structure(); structA.setId(1000L); structA.setMinistereV2(minA);
        structB = new Structure(); structB.setId(2000L); structB.setMinistereV2(minB);

        admin = new ProfilUtilisateur();
        admin.setRole(RoleUtilisateur.ADMIN);
        admin.setKeycloakId(UUID.randomUUID());
        admin.setActif(true);

        dageA = new ProfilUtilisateur();
        dageA.setRole(RoleUtilisateur.DAGE);
        dageA.setMinistere(minA);
        dageA.setKeycloakId(UUID.randomUUID());
        dageA.setActif(true);

        dageB = new ProfilUtilisateur();
        dageB.setRole(RoleUtilisateur.DAGE);
        dageB.setMinistere(minB);
        dageB.setKeycloakId(UUID.randomUUID());
        dageB.setActif(true);

        gestionnaireA = new ProfilUtilisateur();
        gestionnaireA.setRole(RoleUtilisateur.GESTIONNAIRE);
        gestionnaireA.setCohorte(cohorteA);
        gestionnaireA.setStructure(structA);
        gestionnaireA.setKeycloakId(UUID.randomUUID());
        gestionnaireA.setActif(true);

        gestionnaireB = new ProfilUtilisateur();
        gestionnaireB.setRole(RoleUtilisateur.GESTIONNAIRE);
        gestionnaireB.setCohorte(cohorteB);
        gestionnaireB.setStructure(structB);
        gestionnaireB.setKeycloakId(UUID.randomUUID());
        gestionnaireB.setActif(true);

        globalMeeting = createMeeting(MeetingType.GLOBAL, null, "admin", null);
        cohortAMeeting = createMeeting(MeetingType.COHORT, 100L, "admin", null);
        cohortBMeeting = createMeeting(MeetingType.COHORT, 200L, "admin", null);
        structAMeeting = createMeeting(MeetingType.STRUCTURE, 1000L, "admin", null);
        structBMeeting = createMeeting(MeetingType.STRUCTURE, 2000L, "admin", null);
        minAMeeting = createMeeting(MeetingType.MINISTERE, 10L, "admin", null);
        minBMeeting = createMeeting(MeetingType.MINISTERE, 20L, "admin", null);
        directA = createMeeting(MeetingType.DIRECT, null, dageA.getKeycloakId().toString(), null);
        directB = createMeeting(MeetingType.DIRECT, null, "other", List.of(gestionnaireB.getKeycloakId().toString()));
    }

    private Meeting createMeeting(MeetingType type, Long ref, String creator, List<String> parts) {
        Meeting m = new Meeting();
        m.setType(type);
        m.setReferenceId(ref);
        m.setCreatedByUserId(creator);
        m.setRoomId(UUID.randomUUID().toString());
        m.setScheduledAt(LocalDateTime.now().plusDays(1));
        if (parts != null) {
            m.setParticipantIds(parts);
        }
        return meetingRepository.saveAndFlush(m);
    }

    @Test
    public void testAdminAccess() {
        assertTrue(autorisationService.peutLireMeeting(globalMeeting, admin));
        assertTrue(autorisationService.peutLireMeeting(cohortAMeeting, admin));
        assertTrue(autorisationService.peutLireMeeting(structAMeeting, admin));
        assertTrue(autorisationService.peutLireMeeting(minAMeeting, admin));
        assertTrue(autorisationService.peutLireMeeting(directA, admin));

        List<Meeting> list = autorisationService.obtenirMeetingsAccessibles(admin);
        assertTrue(list.containsAll(List.of(globalMeeting, cohortAMeeting, structAMeeting, minAMeeting, directA)));
    }

    @Test
    public void testDageAAccess() {
        assertTrue(autorisationService.peutLireMeeting(globalMeeting, dageA));
        assertTrue(autorisationService.peutLireMeeting(minAMeeting, dageA));
        assertFalse(autorisationService.peutLireMeeting(minBMeeting, dageA));
        assertFalse(autorisationService.peutLireMeeting(cohortAMeeting, dageA));
        assertFalse(autorisationService.peutLireMeeting(structAMeeting, dageA));

        // Direct
        assertTrue(autorisationService.peutLireMeeting(directA, dageA)); // Creator
        assertFalse(autorisationService.peutLireMeeting(directB, dageA)); // Unrelated

        List<Meeting> list = autorisationService.obtenirMeetingsAccessibles(dageA);
        assertTrue(list.contains(globalMeeting));
        assertTrue(list.contains(minAMeeting));
        assertTrue(list.contains(directA));
        assertFalse(list.contains(minBMeeting));
        assertFalse(list.contains(cohortAMeeting));
    }

    @Test
    public void testGestionnaireAAccess() {
        assertTrue(autorisationService.peutLireMeeting(globalMeeting, gestionnaireA));
        assertTrue(autorisationService.peutLireMeeting(cohortAMeeting, gestionnaireA));
        assertFalse(autorisationService.peutLireMeeting(cohortBMeeting, gestionnaireA));
        assertTrue(autorisationService.peutLireMeeting(structAMeeting, gestionnaireA));
        assertFalse(autorisationService.peutLireMeeting(structBMeeting, gestionnaireA));
        assertTrue(autorisationService.peutLireMeeting(minAMeeting, gestionnaireA));
        assertFalse(autorisationService.peutLireMeeting(minBMeeting, gestionnaireA));

        // Direct
        assertFalse(autorisationService.peutLireMeeting(directA, gestionnaireA));

        List<Meeting> list = autorisationService.obtenirMeetingsAccessibles(gestionnaireA);
        assertTrue(list.contains(globalMeeting));
        assertTrue(list.contains(cohortAMeeting));
        assertTrue(list.contains(structAMeeting));
        assertTrue(list.contains(minAMeeting));
        assertFalse(list.contains(cohortBMeeting));
        assertFalse(list.contains(structBMeeting));
    }

    @Test
    public void testDirectParticipant() {
        assertTrue(autorisationService.peutLireMeeting(directB, gestionnaireB));
        List<Meeting> list = autorisationService.obtenirMeetingsAccessibles(gestionnaireB);
        assertTrue(list.contains(directB));
    }

    @Test
    public void testProfileInactive() {
        gestionnaireA.setActif(false);
        assertFalse(autorisationService.peutLireMeeting(globalMeeting, gestionnaireA));
        List<Meeting> list = autorisationService.obtenirMeetingsAccessibles(gestionnaireA);
        assertTrue(list.isEmpty());
    }
}
