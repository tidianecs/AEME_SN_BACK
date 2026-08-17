package com.ditix.backend.Meeting;

import com.ditix.backend.Meeting.DTO.CreateManagedMeetingRequest;
import com.ditix.backend.Meeting.DTO.CreateMeetingRequest;
import com.ditix.backend.Meeting.Model.Meeting;
import com.ditix.backend.Meeting.Model.MeetingType;
import com.ditix.backend.Meeting.Repository.MeetingRepository;
import com.ditix.backend.Meeting.Services.MeetingAdminService;
import com.ditix.backend.Meeting.Services.MeetingService;
import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Repository.CohorteRepository;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MeetingAdminIntegrationTest {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingAdminService meetingAdminService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private CohorteRepository cohorteRepository;
    @Autowired
    private StructureRepository structureRepository;
    @Autowired
    private MinistereRepository ministereRepository;
    @Autowired
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    private ProfilUtilisateur admin;
    private ProfilUtilisateur dage;
    private ProfilUtilisateur gestionnaire;

    private Ministere minActive;
    private Ministere minInactive;
    private Cohorte cohActive;
    private Cohorte cohInactive;
    private Structure structActive;
    private Structure structInactive;

    @BeforeEach
    public void setup() {
        minActive = new Ministere(); minActive.setNom("MinA"); minActive.setCode("MA"); minActive.setActif(true);
        ministereRepository.saveAndFlush(minActive);

        minInactive = new Ministere(); minInactive.setNom("MinI"); minInactive.setCode("MI"); minInactive.setActif(false);
        ministereRepository.saveAndFlush(minInactive);

        cohActive = new Cohorte(); cohActive.setNom("CohA"); cohActive.setCode("CA"); cohActive.setActif(true);
        cohorteRepository.saveAndFlush(cohActive);

        cohInactive = new Cohorte(); cohInactive.setNom("CohI"); cohInactive.setCode("CI"); cohInactive.setActif(false);
        cohorteRepository.saveAndFlush(cohInactive);

        structActive = new Structure(); structActive.setName("StructA"); structActive.setCode("SA"); structActive.setActif(true);
        structureRepository.saveAndFlush(structActive);

        structInactive = new Structure(); structInactive.setName("StructI"); structInactive.setCode("SI"); structInactive.setActif(false);
        structureRepository.saveAndFlush(structInactive);

        admin = new ProfilUtilisateur();
        admin.setRole(RoleUtilisateur.ADMIN);
        admin.setKeycloakId(UUID.randomUUID());
        admin.setActif(true);
        admin.setPrenom("A"); admin.setNom("B"); admin.setEmail(UUID.randomUUID() + "@test.com");
        profilUtilisateurRepository.saveAndFlush(admin);

        dage = new ProfilUtilisateur();
        dage.setRole(RoleUtilisateur.DAGE);
        dage.setMinistere(minActive);
        dage.setKeycloakId(UUID.randomUUID());
        dage.setActif(true);
        dage.setPrenom("A"); dage.setNom("B"); dage.setEmail(UUID.randomUUID() + "@test.com");
        profilUtilisateurRepository.saveAndFlush(dage);

        gestionnaire = new ProfilUtilisateur();
        gestionnaire.setRole(RoleUtilisateur.GESTIONNAIRE);
        gestionnaire.setStructure(structActive);
        gestionnaire.setCohorte(cohActive);
        gestionnaire.setKeycloakId(UUID.randomUUID());
        gestionnaire.setActif(true);
        gestionnaire.setPrenom("A"); gestionnaire.setNom("B"); gestionnaire.setEmail(UUID.randomUUID() + "@test.com");
        profilUtilisateurRepository.saveAndFlush(gestionnaire);
    }

    @Test
    public void testCreateGlobal() {
        CreateManagedMeetingRequest req = new CreateManagedMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        var res = meetingAdminService.createGlobalMeeting(req, admin);

        Meeting m = meetingRepository.findById(res.getId()).orElseThrow();
        assertEquals(MeetingType.GLOBAL, m.getType());
        assertNull(m.getReferenceId());
        assertEquals(admin.getKeycloakId().toString(), m.getCreatedByUserId());
        assertTrue(m.getParticipantIds() == null || m.getParticipantIds().isEmpty());
    }

    @Test
    public void testCreateCohort() {
        CreateManagedMeetingRequest req = new CreateManagedMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        req.setTargetId(cohActive.getId());
        var res = meetingAdminService.createCohortMeeting(req, admin);

        Meeting m = meetingRepository.findById(res.getId()).orElseThrow();
        assertEquals(MeetingType.COHORT, m.getType());
        assertEquals(cohActive.getId(), m.getReferenceId());
        assertEquals(admin.getKeycloakId().toString(), m.getCreatedByUserId());
        assertTrue(m.getParticipantIds() == null || m.getParticipantIds().isEmpty());

        // Inactive cohort
        req.setTargetId(cohInactive.getId());
        assertThrows(ResponseStatusException.class, () -> meetingAdminService.createCohortMeeting(req, admin));

        // Missing cohort
        req.setTargetId(9999L);
        assertThrows(ResponseStatusException.class, () -> meetingAdminService.createCohortMeeting(req, admin));
    }

    @Test
    public void testCreateStructure() {
        CreateManagedMeetingRequest req = new CreateManagedMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        req.setTargetId(structActive.getId());
        var res = meetingAdminService.createStructureMeeting(req, admin);

        Meeting m = meetingRepository.findById(res.getId()).orElseThrow();
        assertEquals(MeetingType.STRUCTURE, m.getType());
        assertEquals(structActive.getId(), m.getReferenceId());

        // Inactive
        req.setTargetId(structInactive.getId());
        assertThrows(ResponseStatusException.class, () -> meetingAdminService.createStructureMeeting(req, admin));
    }

    @Test
    public void testCreateMinistere() {
        CreateManagedMeetingRequest req = new CreateManagedMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        req.setTargetId(minActive.getId());
        var res = meetingAdminService.createMinistereMeeting(req, admin);

        Meeting m = meetingRepository.findById(res.getId()).orElseThrow();
        assertEquals(MeetingType.MINISTERE, m.getType());
        assertEquals(minActive.getId(), m.getReferenceId());

        // Inactive
        req.setTargetId(minInactive.getId());
        assertThrows(ResponseStatusException.class, () -> meetingAdminService.createMinistereMeeting(req, admin));
    }

    @Test
    public void testV1Regression() {
        CreateMeetingRequest req = new CreateMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        req.setParticipantIds(List.of("00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000002"));

        var res = meetingService.createMeeting(req, gestionnaire.getKeycloakId().toString());

        Meeting m = meetingRepository.findById(res.getId()).orElseThrow();
        assertEquals(MeetingType.DIRECT, m.getType());
        assertNull(m.getReferenceId());
        assertEquals(gestionnaire.getKeycloakId().toString(), m.getCreatedByUserId());
        assertTrue(m.getParticipantIds().contains("00000000-0000-0000-0000-000000000001"));
    }

    @Test
    public void testV1ManagedPatchBlocked() {
        CreateManagedMeetingRequest req = new CreateManagedMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        var res = meetingAdminService.createGlobalMeeting(req, admin);

        assertThrows(ResponseStatusException.class, () -> meetingService.updateStatus(res.getId(), "IN_PROGRESS", admin.getKeycloakId().toString()));
    }

    @Test
    public void testV1ManagedDeleteBlocked() {
        CreateManagedMeetingRequest req = new CreateManagedMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        var res = meetingAdminService.createGlobalMeeting(req, admin);

        assertThrows(ResponseStatusException.class, () -> meetingService.deleteMeeting(res.getId(), admin.getKeycloakId().toString()));
    }

    @Test
    public void testV2ManagedPatch() {
        CreateManagedMeetingRequest req = new CreateManagedMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        var res = meetingAdminService.createGlobalMeeting(req, admin);

        var updated = meetingAdminService.updateStatus(res.getId(), "IN_PROGRESS", admin);
        assertEquals(com.ditix.backend.Meeting.Model.MeetingStatus.IN_PROGRESS.name(), updated.getStatus());
    }

    @Test
    public void testV2ManagedDelete() {
        CreateManagedMeetingRequest req = new CreateManagedMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(1));
        var res = meetingAdminService.createGlobalMeeting(req, admin);

        meetingAdminService.deleteMeeting(res.getId(), admin);
        assertFalse(meetingRepository.existsById(res.getId()));
    }
}
