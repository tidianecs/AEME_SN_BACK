package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Repository.CohorteRepository;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public class ProfilUtilisateurIntegrationTest {

    @Autowired
    private ProfilUtilisateurRepository profilRepository;

    @Autowired
    private MinistereRepository ministereRepository;

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private CohorteRepository cohorteRepository;

    private Ministere savedMinistere;
    private Structure savedStructure;
    private Cohorte savedCohorte;

    @BeforeEach
    void setUp() {
        profilRepository.deleteAll();
        structureRepository.deleteAll();
        ministereRepository.deleteAll();
        cohorteRepository.deleteAll();

        Ministere ministere = new Ministere();
        ministere.setNom("Ministère Test");
        ministere.setCode("MT");
        savedMinistere = ministereRepository.saveAndFlush(ministere);

        Structure structure = new Structure();
        structure.setName("Structure Test");
        savedStructure = structureRepository.saveAndFlush(structure);

        Cohorte cohorte = new Cohorte();
        cohorte.setNom("Cohorte Test");
        cohorte.setCode("2024");
        savedCohorte = cohorteRepository.saveAndFlush(cohorte);
    }

    private ProfilUtilisateur createBaseProfil(RoleUtilisateur role) {
        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setKeycloakId(UUID.randomUUID());
        profil.setPrenom("Prenom");
        profil.setNom("Nom");
        profil.setEmail(UUID.randomUUID().toString() + "@test.com");
        profil.setRole(role);
        return profil;
    }

    @Test
    void testAdminValide_Accepte() {
        ProfilUtilisateur admin = createBaseProfil(RoleUtilisateur.ADMIN);
        assertDoesNotThrow(() -> profilRepository.saveAndFlush(admin));
    }

    @Test
    void testAdminAvecMinistere_Rejete() {
        ProfilUtilisateur admin = createBaseProfil(RoleUtilisateur.ADMIN);
        admin.setMinistere(savedMinistere);
        assertThrows(DataIntegrityViolationException.class, () -> profilRepository.saveAndFlush(admin));
    }

    @Test
    void testAdminAvecStructure_Rejete() {
        ProfilUtilisateur admin = createBaseProfil(RoleUtilisateur.ADMIN);
        admin.setStructure(savedStructure);
        assertThrows(DataIntegrityViolationException.class, () -> profilRepository.saveAndFlush(admin));
    }

    @Test
    void testAdminAvecCohorte_Rejete() {
        ProfilUtilisateur admin = createBaseProfil(RoleUtilisateur.ADMIN);
        admin.setCohorte(savedCohorte);
        assertThrows(DataIntegrityViolationException.class, () -> profilRepository.saveAndFlush(admin));
    }

    @Test
    void testDageAvecMinistere_Accepte() {
        ProfilUtilisateur dage = createBaseProfil(RoleUtilisateur.DAGE);
        dage.setMinistere(savedMinistere);
        assertDoesNotThrow(() -> profilRepository.saveAndFlush(dage));
    }

    @Test
    void testDageSansMinistere_Rejete() {
        ProfilUtilisateur dage = createBaseProfil(RoleUtilisateur.DAGE);
        assertThrows(DataIntegrityViolationException.class, () -> profilRepository.saveAndFlush(dage));
    }

    @Test
    void testDageAvecStructure_Rejete() {
        ProfilUtilisateur dage = createBaseProfil(RoleUtilisateur.DAGE);
        dage.setMinistere(savedMinistere);
        dage.setStructure(savedStructure);
        assertThrows(DataIntegrityViolationException.class, () -> profilRepository.saveAndFlush(dage));
    }

    @Test
    void testDageAvecCohorte_Rejete() {
        ProfilUtilisateur dage = createBaseProfil(RoleUtilisateur.DAGE);
        dage.setMinistere(savedMinistere);
        dage.setCohorte(savedCohorte);
        assertThrows(DataIntegrityViolationException.class, () -> profilRepository.saveAndFlush(dage));
    }

    @Test
    void testGestionnaireAvecStructureEtCohorte_Accepte() {
        ProfilUtilisateur gestionnaire = createBaseProfil(RoleUtilisateur.GESTIONNAIRE);
        gestionnaire.setStructure(savedStructure);
        gestionnaire.setCohorte(savedCohorte);
        assertDoesNotThrow(() -> profilRepository.saveAndFlush(gestionnaire));
    }

    @Test
    void testGestionnaireSansStructure_Rejete() {
        ProfilUtilisateur gestionnaire = createBaseProfil(RoleUtilisateur.GESTIONNAIRE);
        gestionnaire.setCohorte(savedCohorte);
        assertThrows(DataIntegrityViolationException.class, () -> profilRepository.saveAndFlush(gestionnaire));
    }

    @Test
    void testGestionnaireSansCohorte_Rejete() {
        ProfilUtilisateur gestionnaire = createBaseProfil(RoleUtilisateur.GESTIONNAIRE);
        gestionnaire.setStructure(savedStructure);
        assertThrows(DataIntegrityViolationException.class, () -> profilRepository.saveAndFlush(gestionnaire));
    }

    @Test
    void testGestionnaireAvecMinistere_Rejete() {
        ProfilUtilisateur gestionnaire = createBaseProfil(RoleUtilisateur.GESTIONNAIRE);
        gestionnaire.setStructure(savedStructure);
        gestionnaire.setCohorte(savedCohorte);
        gestionnaire.setMinistere(savedMinistere);
        assertThrows(DataIntegrityViolationException.class, () -> profilRepository.saveAndFlush(gestionnaire));
    }

    @Test
    void testDeuxDageActifsMemeMinistere_Rejete() {
        ProfilUtilisateur dage1 = createBaseProfil(RoleUtilisateur.DAGE);
        dage1.setMinistere(savedMinistere);
        profilRepository.saveAndFlush(dage1);

        ProfilUtilisateur dage2 = createBaseProfil(RoleUtilisateur.DAGE);
        dage2.setMinistere(savedMinistere);
        assertThrows(DataIntegrityViolationException.class, () -> profilRepository.saveAndFlush(dage2));
    }

    @Test
    void testDageActifEtInactifMemeMinistere_Accepte() {
        ProfilUtilisateur dage1 = createBaseProfil(RoleUtilisateur.DAGE);
        dage1.setMinistere(savedMinistere);
        dage1.setActif(false);
        profilRepository.saveAndFlush(dage1);

        ProfilUtilisateur dage2 = createBaseProfil(RoleUtilisateur.DAGE);
        dage2.setMinistere(savedMinistere);
        dage2.setActif(true);
        assertDoesNotThrow(() -> profilRepository.saveAndFlush(dage2));
    }

    @Test
    void testDeuxGestionnairesMemeStructure_Accepte() {
        ProfilUtilisateur gestionnaire1 = createBaseProfil(RoleUtilisateur.GESTIONNAIRE);
        gestionnaire1.setStructure(savedStructure);
        gestionnaire1.setCohorte(savedCohorte);
        profilRepository.saveAndFlush(gestionnaire1);

        ProfilUtilisateur gestionnaire2 = createBaseProfil(RoleUtilisateur.GESTIONNAIRE);
        gestionnaire2.setStructure(savedStructure);
        gestionnaire2.setCohorte(savedCohorte);
        assertDoesNotThrow(() -> profilRepository.saveAndFlush(gestionnaire2));
    }

    @Test
    void testDeuxGestionnairesCohortesDifferentesMemeStructure_Accepte() {
        Cohorte cohorte2 = new Cohorte();
        cohorte2.setNom("Cohorte 2");
        cohorte2.setCode("2025");
        Cohorte savedCohorte2 = cohorteRepository.saveAndFlush(cohorte2);

        ProfilUtilisateur gestionnaire1 = createBaseProfil(RoleUtilisateur.GESTIONNAIRE);
        gestionnaire1.setStructure(savedStructure);
        gestionnaire1.setCohorte(savedCohorte);
        profilRepository.saveAndFlush(gestionnaire1);

        ProfilUtilisateur gestionnaire2 = createBaseProfil(RoleUtilisateur.GESTIONNAIRE);
        gestionnaire2.setStructure(savedStructure);
        gestionnaire2.setCohorte(savedCohorte2);
        assertDoesNotThrow(() -> profilRepository.saveAndFlush(gestionnaire2));
    }

    @Test
    void testKeycloakIdDuplique_Rejete() {
        UUID commonUuid = UUID.randomUUID();

        ProfilUtilisateur p1 = createBaseProfil(RoleUtilisateur.ADMIN);
        p1.setKeycloakId(commonUuid);
        profilRepository.saveAndFlush(p1);

        ProfilUtilisateur p2 = createBaseProfil(RoleUtilisateur.ADMIN);
        p2.setKeycloakId(commonUuid);
        assertThrows(DataIntegrityViolationException.class, () -> profilRepository.saveAndFlush(p2));
    }

    @Test
    void testEmailDuplique_Rejete() {
        String commonEmail = "test@test.com";

        ProfilUtilisateur p1 = createBaseProfil(RoleUtilisateur.ADMIN);
        p1.setEmail(commonEmail);
        profilRepository.saveAndFlush(p1);

        ProfilUtilisateur p2 = createBaseProfil(RoleUtilisateur.ADMIN);
        p2.setEmail(commonEmail);
        assertThrows(DataIntegrityViolationException.class, () -> profilRepository.saveAndFlush(p2));
    }

    @Test
    void testFindGestionnairesByMinistereId_and_StructureId() {
        Ministere minA = new Ministere();
        minA.setNom("Ministère A");
        minA.setCode("MA");
        Ministere ministereA = ministereRepository.saveAndFlush(minA);

        Ministere minB = new Ministere();
        minB.setNom("Ministère B");
        minB.setCode("MB");
        Ministere ministereB = ministereRepository.saveAndFlush(minB);

        Structure sA1 = new Structure();
        sA1.setName("Structure A1");
        sA1.setMinistereV2(ministereA);
        Structure structureA1 = structureRepository.saveAndFlush(sA1);

        Structure sA2 = new Structure();
        sA2.setName("Structure A2");
        sA2.setMinistereV2(ministereA);
        Structure structureA2 = structureRepository.saveAndFlush(sA2);

        Structure sB1 = new Structure();
        sB1.setName("Structure B1");
        sB1.setMinistereV2(ministereB);
        Structure structureB1 = structureRepository.saveAndFlush(sB1);

        Cohorte c1 = new Cohorte();
        c1.setNom("Cohorte 1");
        c1.setCode("C1");
        Cohorte cohorte1 = cohorteRepository.saveAndFlush(c1);

        Cohorte c2 = new Cohorte();
        c2.setNom("Cohorte 2");
        c2.setCode("C2");
        Cohorte cohorte2 = cohorteRepository.saveAndFlush(c2);

        ProfilUtilisateur g1 = createBaseProfil(RoleUtilisateur.GESTIONNAIRE);
        g1.setStructure(structureA1);
        g1.setCohorte(cohorte1);
        profilRepository.saveAndFlush(g1);

        ProfilUtilisateur g2 = createBaseProfil(RoleUtilisateur.GESTIONNAIRE);
        g2.setStructure(structureA2);
        g2.setCohorte(cohorte2);
        profilRepository.saveAndFlush(g2);

        ProfilUtilisateur g3 = createBaseProfil(RoleUtilisateur.GESTIONNAIRE);
        g3.setStructure(structureB1);
        g3.setCohorte(cohorte1);
        profilRepository.saveAndFlush(g3);

        ProfilUtilisateur dageA = createBaseProfil(RoleUtilisateur.DAGE);
        dageA.setMinistere(ministereA);
        profilRepository.saveAndFlush(dageA);

        ProfilUtilisateur admin = createBaseProfil(RoleUtilisateur.ADMIN);
        profilRepository.saveAndFlush(admin);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);

        // TEST A: findGestionnairesByMinistereId(ministereA.id, pageable)
        org.springframework.data.domain.Page<ProfilUtilisateur> pageMinA = profilRepository.findGestionnairesByMinistereId(ministereA.getId(), pageable);
        assertEquals(2, pageMinA.getTotalElements());
        assertTrue(pageMinA.getContent().stream().anyMatch(p -> p.getId().equals(g1.getId())));
        assertTrue(pageMinA.getContent().stream().anyMatch(p -> p.getId().equals(g2.getId())));

        // TEST B: findGestionnairesByMinistereId(ministereB.id, pageable)
        org.springframework.data.domain.Page<ProfilUtilisateur> pageMinB = profilRepository.findGestionnairesByMinistereId(ministereB.getId(), pageable);
        assertEquals(1, pageMinB.getTotalElements());
        assertEquals(g3.getId(), pageMinB.getContent().get(0).getId());

        // TEST C: findGestionnairesByStructureId(structureA1.id, pageable)
        org.springframework.data.domain.Page<ProfilUtilisateur> pageStructA1 = profilRepository.findGestionnairesByStructureId(structureA1.getId(), pageable);
        assertEquals(1, pageStructA1.getTotalElements());
        assertEquals(g1.getId(), pageStructA1.getContent().get(0).getId());

        // TEST D: findGestionnairesByStructureId(structureA2.id, pageable)
        org.springframework.data.domain.Page<ProfilUtilisateur> pageStructA2 = profilRepository.findGestionnairesByStructureId(structureA2.getId(), pageable);
        assertEquals(1, pageStructA2.getTotalElements());
        assertEquals(g2.getId(), pageStructA2.getContent().get(0).getId());

        // TEST E: findGestionnairesByStructureId(structureB1.id, pageable)
        org.springframework.data.domain.Page<ProfilUtilisateur> pageStructB1 = profilRepository.findGestionnairesByStructureId(structureB1.getId(), pageable);
        assertEquals(1, pageStructB1.getTotalElements());
        assertEquals(g3.getId(), pageStructB1.getContent().get(0).getId());

        // Check LazyInitializationException is NOT thrown for relations due to EntityGraph
        ProfilUtilisateur loadedG1 = pageMinA.getContent().stream().filter(p -> p.getId().equals(g1.getId())).findFirst().get();
        assertNotNull(loadedG1.getStructure());
        assertNotNull(loadedG1.getStructure().getMinistereV2());
        assertNotNull(loadedG1.getCohorte());
    }
}
