package com.ditix.backend.Report;

import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.Report.Model.Report;
import com.ditix.backend.Report.Model.ReportStatus;
import com.ditix.backend.Report.Repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public class ReportPostgresIntegrationTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        profilUtilisateurRepository.deleteAll();
    }

    private ProfilUtilisateur createProfil() {
        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setKeycloakId(UUID.randomUUID());
        profil.setPrenom("Test");
        profil.setNom("User");
        profil.setEmail(UUID.randomUUID().toString() + "@test.com");
        profil.setRole(RoleUtilisateur.ADMIN);
        return profilUtilisateurRepository.saveAndFlush(profil);
    }

    @Test
    void testRelationValide() {
        ProfilUtilisateur profil = createProfil();

        Report report = Report.builder()
                .createdByUserId(profil.getKeycloakId().toString())
                .reportDate(LocalDateTime.now())
                .profilUtilisateur(profil)
                .build();
        report.setReportStatus(ReportStatus.SUBMITTED);

        Report savedReport = reportRepository.saveAndFlush(report);

        assertNotNull(savedReport.getId());
        assertNotNull(savedReport.getProfilUtilisateur());
        assertEquals(profil.getId(), savedReport.getProfilUtilisateur().getId());
    }

    @Test
    void testLegacyReportStillValid() {
        Report report = Report.builder()
                .createdByUserId(UUID.randomUUID().toString())
                .reportDate(LocalDateTime.now())
                .build();
        report.setReportStatus(ReportStatus.SUBMITTED);

        Report savedReport = reportRepository.saveAndFlush(report);

        assertNotNull(savedReport.getId());
        assertNull(savedReport.getProfilUtilisateur());
    }

    @Test
    void testDeleteRestrict_PreventsProfilDeletion() {
        ProfilUtilisateur profil = createProfil();

        Report report = Report.builder()
                .createdByUserId(profil.getKeycloakId().toString())
                .reportDate(LocalDateTime.now())
                .profilUtilisateur(profil)
                .build();
        report.setReportStatus(ReportStatus.SUBMITTED);
        reportRepository.saveAndFlush(report);

        assertThrows(DataIntegrityViolationException.class, () -> {
            profilUtilisateurRepository.delete(profil);
            profilUtilisateurRepository.flush();
        });
    }
}
