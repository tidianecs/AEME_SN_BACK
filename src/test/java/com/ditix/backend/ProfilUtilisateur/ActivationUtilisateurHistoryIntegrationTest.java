package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.Chat.Models.Conversation;
import com.ditix.backend.Chat.Models.ConversationMember;
import com.ditix.backend.Chat.Models.ConversationType;
import com.ditix.backend.Chat.Repository.ConversationMemberRepository;
import com.ditix.backend.Chat.Repository.ConversationRepository;
import com.ditix.backend.Meeting.Model.Meeting;
import com.ditix.backend.Meeting.Model.MeetingStatus;
import com.ditix.backend.Meeting.Model.MeetingType;
import com.ditix.backend.Meeting.Repository.MeetingRepository;
import com.ditix.backend.ProfilUtilisateur.DTO.ActivationUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.ProfilUtilisateur.Services.ActivationUtilisateurLocalService;
import com.ditix.backend.Report.Model.Report;
import com.ditix.backend.Report.Model.ReportStatus;
import com.ditix.backend.Report.Repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@org.springframework.transaction.annotation.Transactional
public class ActivationUtilisateurHistoryIntegrationTest {

    @Autowired
    private ActivationUtilisateurLocalService activationUtilisateurLocalService;

    @Autowired
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    void testHistoryPreservedAndManagedMembershipsDeactivated() {
        ProfilUtilisateur target = new ProfilUtilisateur();
        target.setRole(RoleUtilisateur.ADMIN);
        target.setActif(true);
        target.setKeycloakId(java.util.UUID.randomUUID());
        target.setEmail("target-history-" + System.currentTimeMillis() + "@test.com");
        target.setNom("Target");
        target.setPrenom("Test");
        profilUtilisateurRepository.saveAndFlush(target);

        Report report = new Report();
        report.setCreatedByUserId(target.getId().toString());
        report.setReportDate(LocalDateTime.now());
        report.setProfilUtilisateur(target);
        report.setReportStatus(ReportStatus.SUBMITTED);
        reportRepository.saveAndFlush(report);

        Meeting meeting = new Meeting();
        meeting.setCreatedByUserId(target.getId().toString());
        meeting.setScheduledAt(LocalDateTime.now().plusDays(1));
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meeting.setType(MeetingType.DIRECT);
        meeting.setRoomId("room-history-" + System.currentTimeMillis());

        meetingRepository.saveAndFlush(meeting);

        Conversation directConv = new Conversation();
        directConv.setType(ConversationType.DIRECT);
        directConv.setSystemManaged(false);
        directConv.setUserOneId(target.getKeycloakId().toString());
        directConv.setUserTwoId(java.util.UUID.randomUUID().toString());

        conversationRepository.saveAndFlush(directConv);

        ConversationMember directMember = new ConversationMember();
        directMember.setConversationId(directConv.getId());
        directMember.setUserId(target.getKeycloakId().toString());
        directMember.setActive(true);
        conversationMemberRepository.saveAndFlush(directMember);

        Conversation managedConv = new Conversation();
        managedConv.setType(ConversationType.GLOBAL);
        managedConv.setSystemManaged(true);
        conversationRepository.saveAndFlush(managedConv);

        ConversationMember managedMember = new ConversationMember();
        managedMember.setConversationId(managedConv.getId());
        managedMember.setUserId(target.getKeycloakId().toString());
        managedMember.setActive(true);
        conversationMemberRepository.saveAndFlush(managedMember);

        ActivationUtilisateurRequest req = new ActivationUtilisateurRequest();
        req.setActif(false);

        ProfilUtilisateur adminCourant = new ProfilUtilisateur();
        adminCourant.setRole(RoleUtilisateur.ADMIN);
        adminCourant.setActif(true);
        adminCourant.setKeycloakId(java.util.UUID.randomUUID());
        adminCourant.setEmail("admin-history-" + System.currentTimeMillis() + "@test.com");
        adminCourant.setNom("Admin");
        adminCourant.setPrenom("Test");
        profilUtilisateurRepository.saveAndFlush(adminCourant);

        activationUtilisateurLocalService.processLocalActivation(target.getId(), req);

        ProfilUtilisateur updatedTarget = profilUtilisateurRepository.findById(target.getId()).get();
        assertFalse(updatedTarget.getActif(), "A. ProfilUtilisateur row still exists and actif == false.");

        assertTrue(reportRepository.findById(report.getId()).isPresent(), "B. Report row still exists.");

        assertTrue(meetingRepository.findById(meeting.getId()).isPresent(), "C. Meeting row still exists.");

        entityManager.clear();

        ConversationMember updatedDirectMember = conversationMemberRepository.findById(directMember.getId()).get();
        assertTrue(updatedDirectMember.isActive(), "D. DIRECT conversation membership was NOT deactivated");

        ConversationMember updatedManagedMember = conversationMemberRepository.findById(managedMember.getId()).get();
        assertFalse(updatedManagedMember.isActive(), "E. systemManaged memberships are deactivated");
    }
}
