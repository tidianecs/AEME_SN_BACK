package com.ditix.backend.Report;

import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Repository.CohorteRepository;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.Report.Model.Report;
import com.ditix.backend.Report.Model.ReportStatus;
import com.ditix.backend.Report.Repository.ReportRepository;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReportDageDetailIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

    private Ministere ministere;
    private ProfilUtilisateur dage;
    private ProfilUtilisateur gestionnaire;
    private Report report;

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        reportRepository.deleteAll();
        profilRepository.deleteAll();
        structureRepository.deleteAll();
        cohorteRepository.deleteAll();
        ministereRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        profilRepository.deleteAll();
        structureRepository.deleteAll();
        cohorteRepository.deleteAll();
        ministereRepository.deleteAll();

        Cohorte cohorte = new Cohorte();
        cohorte.setNom("Test Cohorte-" + UUID.randomUUID().toString());
        cohorte.setCode("TEST-COH-" + UUID.randomUUID().toString());
        cohorte = cohorteRepository.save(cohorte);

        ministere = new Ministere();
        ministere.setNom("Ministere Test-" + UUID.randomUUID().toString());
        ministere.setCode("MIN-TEST-" + UUID.randomUUID().toString());
        ministere = ministereRepository.save(ministere);

        Structure structure = new Structure();
        structure.setName("Structure Test");
        structure.setMinistereV2(ministere);
        structure = structureRepository.save(structure);

        gestionnaire = new ProfilUtilisateur();
        gestionnaire.setKeycloakId(UUID.randomUUID());
        gestionnaire.setEmail("gest@test.com");
        gestionnaire.setNom("Gest"); gestionnaire.setPrenom("G");
        gestionnaire.setRole(RoleUtilisateur.GESTIONNAIRE);
        gestionnaire.setStructure(structure);
        gestionnaire.setCohorte(cohorte);
        gestionnaire = profilRepository.save(gestionnaire);

        dage = new ProfilUtilisateur();
        dage.setKeycloakId(UUID.randomUUID());
        dage.setEmail("dage@test.com");
        dage.setNom("Dage"); dage.setPrenom("D");
        dage.setRole(RoleUtilisateur.DAGE);
        dage.setMinistere(ministere);
        dage = profilRepository.save(dage);

        report = Report.builder()
                .createdByUserId(gestionnaire.getKeycloakId().toString())
                .profilUtilisateur(gestionnaire)
                .build();
        report.setReportStatus(ReportStatus.SUBMITTED);
        report = reportRepository.save(report);
    }

    @Test
    void testDageCanReadReportDetail() throws Exception {
        mockMvc.perform(get("/api/v1/reports/" + report.getId())
                .with(jwt().jwt(jwt -> jwt.subject(dage.getKeycloakId().toString()))))
                .andExpect(status().isOk());
    }
}
