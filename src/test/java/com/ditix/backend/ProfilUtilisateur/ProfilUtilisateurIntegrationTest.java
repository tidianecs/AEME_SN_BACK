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
}
