package com.ditix.backend.Report;

import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.Report.Model.Report;
import com.ditix.backend.Report.Model.ReportStatus;
import com.ditix.backend.Report.Repository.ReportRepository;
import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Repository.CohorteRepository;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import com.ditix.backend.Report.Services.ReportService;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReportReadIntegrationTest {

    @Autowired
    private MinistereRepository ministereRepository;

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private ProfilUtilisateurRepository profilRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportService reportService;

    private Ministere ministereA;
    private Ministere ministereB;

    private ProfilUtilisateur dageA;
    private ProfilUtilisateur dageB;

    private ProfilUtilisateur gestionnaireA1;
    private ProfilUtilisateur gestionnaireB1;

    private Report reportA1;
    private Report reportB1;
    private Report legacyReport;

    private Cohorte testCohorte;

    @BeforeEach
    void setUp() {
        testCohorte = new Cohorte();
        testCohorte.setNom("Test Cohorte");
        testCohorte.setCode("TEST-COH");
        testCohorte = cohorteRepository.save(testCohorte);
        ministereA = new Ministere();
        ministereA.setNom("Min A");
        ministereA.setCode("MINA");
        ministereA = ministereRepository.save(ministereA);

        ministereB = new Ministere();
        ministereB.setNom("Min B");
        ministereB.setCode("MINB");
        ministereB = ministereRepository.save(ministereB);

        Structure structureA1 = new Structure();
        structureA1.setName("Struct A1");
        structureA1.setMinistereV2(ministereA);
        structureA1 = structureRepository.save(structureA1);

        Structure structureB1 = new Structure();
        structureB1.setName("Struct B1");
        structureB1.setMinistereV2(ministereB);
        structureB1 = structureRepository.save(structureB1);

        dageA = new ProfilUtilisateur();
        dageA.setKeycloakId(UUID.randomUUID());
        dageA.setEmail("dageA@test.com");
        dageA.setNom("Dage");
        dageA.setPrenom("A");
        dageA.setRole(RoleUtilisateur.DAGE);
        dageA.setMinistere(ministereA);
        dageA = profilRepository.save(dageA);

        dageB = new ProfilUtilisateur();
        dageB.setKeycloakId(UUID.randomUUID());
        dageB.setEmail("dageB@test.com");
        dageB.setNom("Dage");
        dageB.setPrenom("B");
        dageB.setRole(RoleUtilisateur.DAGE);
        dageB.setMinistere(ministereB);
        dageB = profilRepository.save(dageB);

        gestionnaireA1 = new ProfilUtilisateur();
        gestionnaireA1.setKeycloakId(UUID.randomUUID());
        gestionnaireA1.setEmail("gestA1@test.com");
        gestionnaireA1.setNom("Gest");
        gestionnaireA1.setPrenom("A1");
        gestionnaireA1.setRole(RoleUtilisateur.GESTIONNAIRE);
        gestionnaireA1.setStructure(structureA1);
        gestionnaireA1.setCohorte(testCohorte);
        gestionnaireA1 = profilRepository.save(gestionnaireA1);

        gestionnaireB1 = new ProfilUtilisateur();
        gestionnaireB1.setKeycloakId(UUID.randomUUID());
        gestionnaireB1.setEmail("gestB1@test.com");
        gestionnaireB1.setNom("Gest");
        gestionnaireB1.setPrenom("B1");
        gestionnaireB1.setRole(RoleUtilisateur.GESTIONNAIRE);
        gestionnaireB1.setStructure(structureB1);
        gestionnaireB1.setCohorte(testCohorte);
        gestionnaireB1 = profilRepository.save(gestionnaireB1);

        reportA1 = Report.builder()
                .createdByUserId(gestionnaireA1.getKeycloakId().toString())
                .profilUtilisateur(gestionnaireA1)
                .build();
        reportA1.setReportStatus(ReportStatus.SUBMITTED);
        reportA1 = reportRepository.save(reportA1);

        reportB1 = Report.builder()
                .createdByUserId(gestionnaireB1.getKeycloakId().toString())
                .profilUtilisateur(gestionnaireB1)
                .build();
        reportB1.setReportStatus(ReportStatus.SUBMITTED);
        reportB1 = reportRepository.save(reportB1);

        legacyReport = Report.builder()
                .createdByUserId("legacy-id")
                .build();
        legacyReport.setReportStatus(ReportStatus.SUBMITTED);
        legacyReport = reportRepository.save(legacyReport);
    }

    @Test
    void dageCanReadReportsFromTheirMinistryOnly() {
        var reports = reportService.getAllReports(dageA);
        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).getId()).isEqualTo(reportA1.getId());
    }

    @Test
    void gestionnaireCanReadOwnReportsOnly() {
        var reports = reportService.getAllReports(gestionnaireA1);
        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).getId()).isEqualTo(reportA1.getId());
    }
}
