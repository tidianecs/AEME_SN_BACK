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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReportDownloadIntegrationTest {

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

    private Ministere ministereA;
    private Ministere ministereB;
    private ProfilUtilisateur dageA;
    private ProfilUtilisateur dageB;
    private ProfilUtilisateur gestionnaireA;
    private ProfilUtilisateur admin;
    private Report reportA;

    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create real temporary file
        tempFile = Files.createTempFile("test-report-file", ".pdf");
        Files.writeString(tempFile, "Test PDF content");

        ministereA = new Ministere();
        ministereA.setNom("Ministry A " + UUID.randomUUID().toString());
        ministereA.setCode("MIN-A-" + UUID.randomUUID().toString());
        ministereA.setActif(true);
        ministereA = ministereRepository.save(ministereA);

        ministereB = new Ministere();
        ministereB.setNom("Ministry B " + UUID.randomUUID().toString());
        ministereB.setCode("MIN-B-" + UUID.randomUUID().toString());
        ministereB.setActif(true);
        ministereB = ministereRepository.save(ministereB);

        Structure structureA = new Structure();
        structureA.setName("Structure A " + UUID.randomUUID().toString());
        structureA.setMinistereV2(ministereA);
        structureA.setActif(true);
        structureA = structureRepository.save(structureA);

        Cohorte cohorte = new Cohorte();
        cohorte.setNom("Cohorte " + UUID.randomUUID().toString());
        cohorte.setCode("COH-" + UUID.randomUUID().toString());
        cohorte.setActif(true);
        cohorte = cohorteRepository.save(cohorte);

        dageA = new ProfilUtilisateur();
        dageA.setPrenom("Dage");
        dageA.setNom("A");
        dageA.setEmail("dageA." + UUID.randomUUID().toString() + "@example.com");
        dageA.setKeycloakId(UUID.randomUUID());
        dageA.setRole(RoleUtilisateur.DAGE);
        dageA.setActif(true);
        dageA.setMinistere(ministereA);
        dageA = profilRepository.save(dageA);

        dageB = new ProfilUtilisateur();
        dageB.setPrenom("Dage");
        dageB.setNom("B");
        dageB.setEmail("dageB." + UUID.randomUUID().toString() + "@example.com");
        dageB.setKeycloakId(UUID.randomUUID());
        dageB.setRole(RoleUtilisateur.DAGE);
        dageB.setActif(true);
        dageB.setMinistere(ministereB);
        dageB = profilRepository.save(dageB);

        gestionnaireA = new ProfilUtilisateur();
        gestionnaireA.setPrenom("Gestionnaire");
        gestionnaireA.setNom("A");
        gestionnaireA.setEmail("gestA." + UUID.randomUUID().toString() + "@example.com");
        gestionnaireA.setKeycloakId(UUID.randomUUID());
        gestionnaireA.setRole(RoleUtilisateur.GESTIONNAIRE);
        gestionnaireA.setActif(true);
        gestionnaireA.setStructure(structureA);
        gestionnaireA.setCohorte(cohorte);
        gestionnaireA = profilRepository.save(gestionnaireA);

        admin = new ProfilUtilisateur();
        admin.setPrenom("Admin");
        admin.setNom("System");
        admin.setEmail("admin." + UUID.randomUUID().toString() + "@example.com");
        admin.setKeycloakId(UUID.randomUUID());
        admin.setRole(RoleUtilisateur.ADMIN);
        admin.setActif(true);
        admin = profilRepository.save(admin);

        reportA = new Report();
        reportA.setProfilUtilisateur(gestionnaireA);
        reportA.setCreatedByUserId(gestionnaireA.getKeycloakId().toString());
        reportA.setReportStatus(ReportStatus.SUBMITTED);
        
        // Attach the temp file
        reportA.setPieceJustificativeModificationPath(tempFile.toAbsolutePath().toString());
        reportA.setPieceJustificativeModificationName("justif.pdf");

        reportA = reportRepository.save(reportA);
    }

    @AfterEach
    void tearDown() throws IOException {
        reportRepository.deleteAll();
        profilRepository.deleteAll();
        structureRepository.deleteAll();
        cohorteRepository.deleteAll();
        ministereRepository.deleteAll();

        if (tempFile != null && Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
    }

    @Test
    void sameMinistryDageCanDownload() throws Exception {
        mockMvc.perform(get("/api/v1/reports/" + reportA.getId() + "/download/pieceJustificativeModification")
                .with(jwt().jwt(builder -> builder.claim("email", dageA.getEmail()).subject(dageA.getKeycloakId().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_dage"))))
                .andExpect(status().isOk());
    }

    @Test
    void otherMinistryDageCannotDownload() throws Exception {
        mockMvc.perform(get("/api/v1/reports/" + reportA.getId() + "/download/pieceJustificativeModification")
                .with(jwt().jwt(builder -> builder.claim("email", dageB.getEmail()).subject(dageB.getKeycloakId().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_dage"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanDownload() throws Exception {
        mockMvc.perform(get("/api/v1/reports/" + reportA.getId() + "/download/pieceJustificativeModification")
                .with(jwt().jwt(builder -> builder.claim("email", admin.getEmail()).subject(admin.getKeycloakId().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isOk());
    }

    @Test
    void gestionnaireOwnerCanDownload() throws Exception {
        mockMvc.perform(get("/api/v1/reports/" + reportA.getId() + "/download/pieceJustificativeModification")
                .with(jwt().jwt(builder -> builder.claim("email", gestionnaireA.getEmail()).subject(gestionnaireA.getKeycloakId().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_gestionnaire"))))
                .andExpect(status().isOk());
    }
}
