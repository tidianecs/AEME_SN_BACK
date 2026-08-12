package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProfilUtilisateurRepositoryIntegrationTest {

    @Autowired
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    @Test
    void findAllAdminsForUpdate_shouldExecuteWithoutLockingErrors() {
        // Prepare some data
        ProfilUtilisateur admin = new ProfilUtilisateur();
        admin.setRole(RoleUtilisateur.ADMIN);
        admin.setActif(true);
        admin.setKeycloakId(java.util.UUID.randomUUID());
        admin.setEmail("admin-lock-test@test.com");
        admin.setNom("Test");
        admin.setPrenom("Admin");
        profilUtilisateurRepository.saveAndFlush(admin);

        assertDoesNotThrow(() -> {
            List<ProfilUtilisateur> lockedAdmins = profilUtilisateurRepository.findAllAdminsForUpdate();
            assertTrue(lockedAdmins.size() >= 1);
        });
    }
}
