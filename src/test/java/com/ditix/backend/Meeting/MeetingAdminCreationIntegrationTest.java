package com.ditix.backend.Meeting;

import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Repository.CohorteRepository;
import com.ditix.backend.Meeting.DTO.CreateManagedMeetingRequest;
import com.ditix.backend.Meeting.Services.MeetingAdminService;
import com.ditix.backend.Notification.Repository.NotificationRepository;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class MeetingAdminCreationIntegrationTest {

    @Autowired
    private MeetingAdminService meetingAdminService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private com.ditix.backend.Ministere.Repository.MinistereRepository ministereRepository;

    @Autowired
    private com.ditix.backend.Structure.Repository.StructureRepository structureRepository;

    private ProfilUtilisateur createAdmin(String prefix) {
        ProfilUtilisateur admin = new ProfilUtilisateur();
        admin.setKeycloakId(UUID.randomUUID());
        admin.setPrenom("Admin");
        admin.setNom(prefix);
        admin.setEmail(prefix + "@test.com");
        admin.setRole(RoleUtilisateur.ADMIN);
        admin.setActif(true);
        return profilUtilisateurRepository.saveAndFlush(admin);
    }

    @Test
    public void testGlobalMeetingCreationAndNotification() {
        ProfilUtilisateur admin = createAdmin("adminGlobal");
        ProfilUtilisateur participant1 = createAdmin("partGlobal1");
        ProfilUtilisateur participant2 = createAdmin("partGlobal2");

        ProfilUtilisateur inactive = new ProfilUtilisateur();
        inactive.setKeycloakId(UUID.randomUUID());
        inactive.setPrenom("Inactive");
        inactive.setNom("Global");
        inactive.setEmail("inactive_global@test.com");
        inactive.setRole(RoleUtilisateur.ADMIN);
        inactive.setActif(false);
        profilUtilisateurRepository.saveAndFlush(inactive);

        CreateManagedMeetingRequest req = new CreateManagedMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(2));

        var response = meetingAdminService.createGlobalMeeting(req, admin);

        assertEquals(com.ditix.backend.Meeting.Model.MeetingType.GLOBAL, response.getType());
        org.junit.jupiter.api.Assertions.assertNull(response.getReferenceId());
        assertEquals(admin.getKeycloakId().toString(), response.getCreatedByUserId());

        var notifs1 = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(participant1.getId());
        assertEquals(1, notifs1.size());

        var notifs2 = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(participant2.getId());
        assertEquals(1, notifs2.size());

        var notifsAdmin = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(admin.getId());
        assertEquals(0, notifsAdmin.size());

        var notifsInactive = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(inactive.getId());
        assertEquals(0, notifsInactive.size());
    }

    @Test
    public void testMinistereMeetingCreationAndNotification() {
        ProfilUtilisateur admin = createAdmin("adminMin");

        com.ditix.backend.Ministere.Model.Ministere ministereTarget = new com.ditix.backend.Ministere.Model.Ministere();
        ministereTarget.setNom("Min Target");
        ministereTarget.setCode("MT");
        ministereTarget.setActif(true);
        ministereRepository.saveAndFlush(ministereTarget);

        com.ditix.backend.Ministere.Model.Ministere ministereOther = new com.ditix.backend.Ministere.Model.Ministere();
        ministereOther.setNom("Min Other");
        ministereOther.setCode("MO");
        ministereOther.setActif(true);
        ministereRepository.saveAndFlush(ministereOther);

        com.ditix.backend.Structure.Model.Structure structureTarget = new com.ditix.backend.Structure.Model.Structure();
        structureTarget.setName("Struct Target");
        structureTarget.setMinistereV2(ministereTarget);
        structureTarget.setActif(true);
        structureRepository.saveAndFlush(structureTarget);

        com.ditix.backend.Structure.Model.Structure structureOther = new com.ditix.backend.Structure.Model.Structure();
        structureOther.setName("Struct Other");
        structureOther.setMinistereV2(ministereOther);
        structureOther.setActif(true);
        structureRepository.saveAndFlush(structureOther);

        // A. active DAGE in target ministere
        ProfilUtilisateur activeDageTarget = new ProfilUtilisateur();
        activeDageTarget.setKeycloakId(UUID.randomUUID());
        activeDageTarget.setPrenom("A");
        activeDageTarget.setNom("DageTarget");
        activeDageTarget.setEmail("a_dage_target@test.com");
        activeDageTarget.setRole(RoleUtilisateur.DAGE);
        activeDageTarget.setMinistere(ministereTarget);
        activeDageTarget.setActif(true);
        profilUtilisateurRepository.saveAndFlush(activeDageTarget);

        // B. active DAGE in another ministere
        ProfilUtilisateur activeDageOther = new ProfilUtilisateur();
        activeDageOther.setKeycloakId(UUID.randomUUID());
        activeDageOther.setPrenom("B");
        activeDageOther.setNom("DageOther");
        activeDageOther.setEmail("b_dage_other@test.com");
        activeDageOther.setRole(RoleUtilisateur.DAGE);
        activeDageOther.setMinistere(ministereOther);
        activeDageOther.setActif(true);
        profilUtilisateurRepository.saveAndFlush(activeDageOther);

        // C. inactive DAGE in target ministere
        ProfilUtilisateur inactiveDageTarget = new ProfilUtilisateur();
        inactiveDageTarget.setKeycloakId(UUID.randomUUID());
        inactiveDageTarget.setPrenom("C");
        inactiveDageTarget.setNom("InactiveDageTarget");
        inactiveDageTarget.setEmail("c_inactive_dage_target@test.com");
        inactiveDageTarget.setRole(RoleUtilisateur.DAGE);
        inactiveDageTarget.setMinistere(ministereTarget);
        inactiveDageTarget.setActif(true);
        inactiveDageTarget.setActif(false); // set to false
        profilUtilisateurRepository.saveAndFlush(inactiveDageTarget);

        com.ditix.backend.Cohorte.Model.Cohorte dummyCohorte = new com.ditix.backend.Cohorte.Model.Cohorte();
        dummyCohorte.setNom("Dummy Cohort " + UUID.randomUUID());
        dummyCohorte.setCode("DC" + UUID.randomUUID().toString().substring(0,4));
        dummyCohorte.setActif(true);
        cohorteRepository.saveAndFlush(dummyCohorte);

        // D. active GESTIONNAIRE whose structure belongs to target ministere
        ProfilUtilisateur activeGestTarget = new ProfilUtilisateur();
        activeGestTarget.setKeycloakId(UUID.randomUUID());
        activeGestTarget.setPrenom("D");
        activeGestTarget.setNom("GestTarget");
        activeGestTarget.setEmail("d_gest_target@test.com" + UUID.randomUUID());
        activeGestTarget.setRole(RoleUtilisateur.GESTIONNAIRE);
        activeGestTarget.setStructure(structureTarget);
        activeGestTarget.setCohorte(dummyCohorte);
        activeGestTarget.setActif(true);
        profilUtilisateurRepository.saveAndFlush(activeGestTarget);

        // E. active GESTIONNAIRE whose structure belongs to another ministere
        ProfilUtilisateur activeGestOther = new ProfilUtilisateur();
        activeGestOther.setKeycloakId(UUID.randomUUID());
        activeGestOther.setPrenom("E");
        activeGestOther.setNom("GestOther");
        activeGestOther.setEmail("e_gest_other@test.com" + UUID.randomUUID());
        activeGestOther.setRole(RoleUtilisateur.GESTIONNAIRE);
        activeGestOther.setStructure(structureOther);
        activeGestOther.setCohorte(dummyCohorte);
        activeGestOther.setActif(true);
        profilUtilisateurRepository.saveAndFlush(activeGestOther);

        // F. inactive GESTIONNAIRE in target ministere structure
        ProfilUtilisateur inactiveGestTarget = new ProfilUtilisateur();
        inactiveGestTarget.setKeycloakId(UUID.randomUUID());
        inactiveGestTarget.setPrenom("F");
        inactiveGestTarget.setNom("InactiveGestTarget");
        inactiveGestTarget.setEmail("f_inactive_gest_target@test.com" + UUID.randomUUID());
        inactiveGestTarget.setRole(RoleUtilisateur.GESTIONNAIRE);
        inactiveGestTarget.setStructure(structureTarget);
        inactiveGestTarget.setCohorte(dummyCohorte);
        inactiveGestTarget.setActif(false);
        profilUtilisateurRepository.saveAndFlush(inactiveGestTarget);

        CreateManagedMeetingRequest req = new CreateManagedMeetingRequest();
        req.setTargetId(ministereTarget.getId());
        req.setScheduledAt(LocalDateTime.now().plusDays(2));

        var response = meetingAdminService.createMinistereMeeting(req, admin);

        assertEquals(com.ditix.backend.Meeting.Model.MeetingType.MINISTERE, response.getType());
        assertEquals(ministereTarget.getId(), response.getReferenceId());

        // Assertions
        assertEquals(1, notificationRepository.findByRecipientIdOrderByCreatedAtDesc(activeDageTarget.getId()).size());
        assertEquals(0, notificationRepository.findByRecipientIdOrderByCreatedAtDesc(activeDageOther.getId()).size());
        assertEquals(0, notificationRepository.findByRecipientIdOrderByCreatedAtDesc(inactiveDageTarget.getId()).size());

        assertEquals(1, notificationRepository.findByRecipientIdOrderByCreatedAtDesc(activeGestTarget.getId()).size());
        assertEquals(0, notificationRepository.findByRecipientIdOrderByCreatedAtDesc(activeGestOther.getId()).size());
        assertEquals(0, notificationRepository.findByRecipientIdOrderByCreatedAtDesc(inactiveGestTarget.getId()).size());

        assertEquals(0, notificationRepository.findByRecipientIdOrderByCreatedAtDesc(admin.getId()).size());
    }

    @Test
    public void testStructureAndCohortMeetingCreationAndNotification() {
        ProfilUtilisateur admin = createAdmin("adminStruct");

        Cohorte cohorte = new Cohorte();
        cohorte.setNom("Cohorte 1 " + UUID.randomUUID());
        cohorte.setCode("C1" + UUID.randomUUID().toString().substring(0,4));
        cohorte.setActif(true);
        cohorteRepository.saveAndFlush(cohorte);

        com.ditix.backend.Ministere.Model.Ministere ministere1 = new com.ditix.backend.Ministere.Model.Ministere();
        ministere1.setNom("Min 1");
        ministere1.setCode("MX");
        ministere1.setActif(true);
        ministereRepository.saveAndFlush(ministere1);

        com.ditix.backend.Structure.Model.Structure structure = new com.ditix.backend.Structure.Model.Structure();
        structure.setName("Struct 1");
        structure.setMinistereV2(ministere1);
        structure.setActif(true);
        structureRepository.saveAndFlush(structure);

        ProfilUtilisateur gestionnaire = new ProfilUtilisateur();
        gestionnaire.setKeycloakId(UUID.randomUUID());
        gestionnaire.setPrenom("Gest");
        gestionnaire.setNom("Struct1");
        gestionnaire.setEmail("gest1@test.com");
        gestionnaire.setRole(RoleUtilisateur.GESTIONNAIRE);
        gestionnaire.setStructure(structure);
        gestionnaire.setCohorte(cohorte);
        gestionnaire.setActif(true);
        profilUtilisateurRepository.saveAndFlush(gestionnaire);

        // STRUCTURE test
        CreateManagedMeetingRequest reqS = new CreateManagedMeetingRequest();
        reqS.setTargetId(structure.getId());
        reqS.setScheduledAt(LocalDateTime.now().plusDays(2));
        var resS = meetingAdminService.createStructureMeeting(reqS, admin);
        assertEquals(com.ditix.backend.Meeting.Model.MeetingType.STRUCTURE, resS.getType());
        assertEquals(structure.getId(), resS.getReferenceId());

        // COHORT test
        CreateManagedMeetingRequest reqC = new CreateManagedMeetingRequest();
        reqC.setTargetId(cohorte.getId());
        reqC.setScheduledAt(LocalDateTime.now().plusDays(2));
        var resC = meetingAdminService.createCohortMeeting(reqC, admin);
        assertEquals(com.ditix.backend.Meeting.Model.MeetingType.COHORT, resC.getType());
        assertEquals(cohorte.getId(), resC.getReferenceId());

        var notifsGest = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(gestionnaire.getId());
        assertEquals(2, notifsGest.size());
    }
}
