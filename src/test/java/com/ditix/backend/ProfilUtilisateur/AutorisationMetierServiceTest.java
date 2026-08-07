package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.AutorisationMetierService;
import com.ditix.backend.Structure.Model.Structure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AutorisationMetierServiceTest {

    private AutorisationMetierService autorisationMetierService;

    @BeforeEach
    void setUp() {
        autorisationMetierService = new AutorisationMetierService();
    }

    private ProfilUtilisateur createProfil(RoleUtilisateur role) {
        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setRole(role);
        return profil;
    }

    private Ministere createMinistere(Long id) {
        Ministere m = new Ministere();
        m.setId(id);
        return m;
    }

    private Structure createStructure(Long id, Ministere ministere) {
        Structure s = new Structure();
        s.setId(id);
        s.setMinistereV2(ministere);
        return s;
    }

    @Test
    void testEstAdmin() {
        assertTrue(autorisationMetierService.estAdmin(createProfil(RoleUtilisateur.ADMIN)));
        assertFalse(autorisationMetierService.estAdmin(createProfil(RoleUtilisateur.DAGE)));
        assertFalse(autorisationMetierService.estAdmin(createProfil(RoleUtilisateur.GESTIONNAIRE)));
        assertFalse(autorisationMetierService.estAdmin(null));
    }

    @Test
    void testEstDage() {
        assertTrue(autorisationMetierService.estDage(createProfil(RoleUtilisateur.DAGE)));
        assertFalse(autorisationMetierService.estDage(createProfil(RoleUtilisateur.ADMIN)));
        assertFalse(autorisationMetierService.estDage(createProfil(RoleUtilisateur.GESTIONNAIRE)));
        assertFalse(autorisationMetierService.estDage(null));
    }

    @Test
    void testEstGestionnaire() {
        assertTrue(autorisationMetierService.estGestionnaire(createProfil(RoleUtilisateur.GESTIONNAIRE)));
        assertFalse(autorisationMetierService.estGestionnaire(createProfil(RoleUtilisateur.ADMIN)));
        assertFalse(autorisationMetierService.estGestionnaire(createProfil(RoleUtilisateur.DAGE)));
        assertFalse(autorisationMetierService.estGestionnaire(null));
    }

    @Test
    void testAAccesMinistere_Admin() {
        ProfilUtilisateur admin = createProfil(RoleUtilisateur.ADMIN);
        assertTrue(autorisationMetierService.aAccesMinistere(admin, 1L));
        assertTrue(autorisationMetierService.aAccesMinistere(admin, 99L));
    }

    @Test
    void testAAccesMinistere_Dage() {
        ProfilUtilisateur dage = createProfil(RoleUtilisateur.DAGE);
        Ministere ministere = createMinistere(1L);
        dage.setMinistere(ministere);

        assertTrue(autorisationMetierService.aAccesMinistere(dage, 1L));
        assertFalse(autorisationMetierService.aAccesMinistere(dage, 2L));
    }

    @Test
    void testAAccesMinistere_Gestionnaire() {
        ProfilUtilisateur gestionnaire = createProfil(RoleUtilisateur.GESTIONNAIRE);
        Ministere ministere = createMinistere(1L);
        Structure structure = createStructure(10L, ministere);
        gestionnaire.setStructure(structure);

        assertTrue(autorisationMetierService.aAccesMinistere(gestionnaire, 1L));
        assertFalse(autorisationMetierService.aAccesMinistere(gestionnaire, 2L));
    }

    @Test
    void testAAccesStructure_Admin() {
        ProfilUtilisateur admin = createProfil(RoleUtilisateur.ADMIN);
        Structure structure = createStructure(10L, createMinistere(1L));

        assertTrue(autorisationMetierService.aAccesStructure(admin, structure));
    }

    @Test
    void testAAccesStructure_Dage() {
        ProfilUtilisateur dage = createProfil(RoleUtilisateur.DAGE);
        dage.setMinistere(createMinistere(1L));

        Structure structureAutorisee = createStructure(10L, createMinistere(1L));
        Structure structureRefusee = createStructure(20L, createMinistere(2L));

        assertTrue(autorisationMetierService.aAccesStructure(dage, structureAutorisee));
        assertFalse(autorisationMetierService.aAccesStructure(dage, structureRefusee));
    }

    @Test
    void testAAccesStructure_Gestionnaire() {
        ProfilUtilisateur gestionnaire = createProfil(RoleUtilisateur.GESTIONNAIRE);
        Structure structureAssignee = createStructure(10L, createMinistere(1L));
        gestionnaire.setStructure(structureAssignee);

        Structure structureRefusee = createStructure(20L, createMinistere(1L));

        assertTrue(autorisationMetierService.aAccesStructure(gestionnaire, structureAssignee));
        assertFalse(autorisationMetierService.aAccesStructure(gestionnaire, structureRefusee));
    }

    @Test
    void testAAcces_NullSafety() {
        assertFalse(autorisationMetierService.aAccesMinistere(null, 1L));
        assertFalse(autorisationMetierService.aAccesMinistere(createProfil(RoleUtilisateur.ADMIN), null));

        assertFalse(autorisationMetierService.aAccesStructure(null, new Structure()));
        assertFalse(autorisationMetierService.aAccesStructure(createProfil(RoleUtilisateur.ADMIN), null));
    }
}
