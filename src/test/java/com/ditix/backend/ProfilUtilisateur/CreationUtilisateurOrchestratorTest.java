package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Repository.CohorteRepository;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import com.ditix.backend.ProfilUtilisateur.DTO.CreerUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.DTO.CreationUtilisateurResponse;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.ProfilUtilisateur.Services.CreationUtilisateurOrchestrator;
import com.ditix.backend.ProfilUtilisateur.Services.GestionCompteKeycloakService;
import com.ditix.backend.ProfilUtilisateur.Services.SauvegardeProfilService;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreationUtilisateurOrchestratorTest {

    @Mock
    private GestionCompteKeycloakService gestionCompteKeycloakService;

    @Mock
    private SauvegardeProfilService sauvegardeProfilService;

    @Mock
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    @Mock
    private MinistereRepository ministereRepository;

    @Mock
    private StructureRepository structureRepository;

    @Mock
    private CohorteRepository cohorteRepository;

    @Mock
    private com.ditix.backend.Chat.Services.ChatUserMembershipService chatUserMembershipService;

    @InjectMocks
    private CreationUtilisateurOrchestrator orchestrator;

    private CreerUtilisateurRequest request;

    @BeforeEach
    void setUp() {
        request = new CreerUtilisateurRequest();
        request.setPrenom("Test");
        request.setNom("User");
        request.setEmail("test@aeme.sn");
    }

    @Test
    void testAdminValide() {
        request.setRole(RoleUtilisateur.ADMIN);

        UUID keycloakId = UUID.randomUUID();
        when(gestionCompteKeycloakService.creerIdentite(any(), any(), any(), eq("admin"))).thenReturn(keycloakId);

        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setId(1L);
        when(sauvegardeProfilService.sauvegarder(any(), any(), any(), any(), any())).thenReturn(profil);

        CreationUtilisateurResponse response = orchestrator.creerUtilisateur(request);

        assertTrue(response.isInvitationEnvoyee());
        verify(gestionCompteKeycloakService).envoyerActionsInitiales(keycloakId);
    }

    @Test
    void testDageValide() {
        request.setRole(RoleUtilisateur.DAGE);
        request.setMinistereId(10L);

        Ministere ministere = new Ministere();
        ministere.setId(10L);
        ministere.setActif(true);

        when(ministereRepository.findById(10L)).thenReturn(Optional.of(ministere));
        when(profilUtilisateurRepository.existsByRoleAndMinistereIdAndActifTrue(RoleUtilisateur.DAGE, 10L)).thenReturn(false);

        UUID keycloakId = UUID.randomUUID();
        when(gestionCompteKeycloakService.creerIdentite(any(), any(), any(), eq("user"))).thenReturn(keycloakId);

        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setId(1L);
        when(sauvegardeProfilService.sauvegarder(any(), any(), eq(ministere), any(), any())).thenReturn(profil);

        CreationUtilisateurResponse response = orchestrator.creerUtilisateur(request);
        assertTrue(response.isInvitationEnvoyee());
    }

    @Test
    void testEmailDejaUtilisePostgres() {
        request.setRole(RoleUtilisateur.ADMIN);
        when(profilUtilisateurRepository.findByEmailIgnoreCase("test@aeme.sn")).thenReturn(Optional.of(new ProfilUtilisateur()));

        assertThrows(ResponseStatusException.class, () -> orchestrator.creerUtilisateur(request));
    }

    @Test
    void testEmailDejaUtiliseKeycloak() {
        request.setRole(RoleUtilisateur.ADMIN);
        when(gestionCompteKeycloakService.emailExiste("test@aeme.sn")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> orchestrator.creerUtilisateur(request));
    }

    @Test
    void testDageDoublon() {
        request.setRole(RoleUtilisateur.DAGE);
        request.setMinistereId(10L);

        Ministere ministere = new Ministere();
        ministere.setId(10L);
        ministere.setActif(true);

        when(ministereRepository.findById(10L)).thenReturn(Optional.of(ministere));
        when(profilUtilisateurRepository.existsByRoleAndMinistereIdAndActifTrue(RoleUtilisateur.DAGE, 10L)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> orchestrator.creerUtilisateur(request));
    }

    @Test
    void testPostgresFailureCompensation() {
        request.setRole(RoleUtilisateur.ADMIN);

        UUID keycloakId = UUID.randomUUID();
        when(gestionCompteKeycloakService.creerIdentite(any(), any(), any(), any())).thenReturn(keycloakId);

        when(sauvegardeProfilService.sauvegarder(any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class, () -> orchestrator.creerUtilisateur(request));

        verify(gestionCompteKeycloakService).supprimerIdentite(keycloakId);
    }

    @Test
    void testPostgresFailureEtCompensationFailure() {
        request.setRole(RoleUtilisateur.ADMIN);

        UUID keycloakId = UUID.randomUUID();
        when(gestionCompteKeycloakService.creerIdentite(any(), any(), any(), any())).thenReturn(keycloakId);

        RuntimeException originalException = new RuntimeException("Original DB Error");
        when(sauvegardeProfilService.sauvegarder(any(), any(), any(), any(), any()))
            .thenThrow(originalException);

        doThrow(new RuntimeException("Compensation Error")).when(gestionCompteKeycloakService).supprimerIdentite(keycloakId);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> orchestrator.creerUtilisateur(request));
        assertEquals("Original DB Error", thrown.getMessage());

        verify(gestionCompteKeycloakService).supprimerIdentite(keycloakId);
    }

    @Test
    void testEmailFailure() {
        request.setRole(RoleUtilisateur.ADMIN);

        UUID keycloakId = UUID.randomUUID();
        when(gestionCompteKeycloakService.creerIdentite(any(), any(), any(), any())).thenReturn(keycloakId);

        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setId(1L);
        when(sauvegardeProfilService.sauvegarder(any(), any(), any(), any(), any())).thenReturn(profil);

        doThrow(new RuntimeException("Email sending failed")).when(gestionCompteKeycloakService).envoyerActionsInitiales(keycloakId);

        CreationUtilisateurResponse response = orchestrator.creerUtilisateur(request);

        assertFalse(response.isInvitationEnvoyee());
        verify(gestionCompteKeycloakService, never()).supprimerIdentite(any());
    }

    @Test
    void testCreerGestionnaireValide() {
        request.setRole(RoleUtilisateur.GESTIONNAIRE);
        request.setMinistereId(null);
        request.setStructureId(100L);
        request.setCohorteId(200L);

        Ministere ministereV2 = new Ministere();
        ministereV2.setId(1L);
        ministereV2.setActif(true);

        Structure structure = new Structure();
        structure.setId(100L);
        structure.setActif(true);
        structure.setMinistereV2(ministereV2);

        Cohorte cohorte = new Cohorte();
        cohorte.setId(200L);
        cohorte.setActif(true);

        when(structureRepository.findById(100L)).thenReturn(Optional.of(structure));
        when(cohorteRepository.findById(200L)).thenReturn(Optional.of(cohorte));

        UUID keycloakId = UUID.randomUUID();
        when(gestionCompteKeycloakService.creerIdentite(any(), any(), any(), eq("user"))).thenReturn(keycloakId);

        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setId(2L);
        when(sauvegardeProfilService.sauvegarder(any(), any(), isNull(), eq(structure), eq(cohorte))).thenReturn(profil);

        CreationUtilisateurResponse response = orchestrator.creerUtilisateur(request);

        assertTrue(response.isInvitationEnvoyee());
        verify(gestionCompteKeycloakService).envoyerActionsInitiales(keycloakId);
    }

    @Test
    void testDageMinistereAbsent() {
        request.setRole(RoleUtilisateur.DAGE);
        request.setMinistereId(10L);
        when(ministereRepository.findById(10L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orchestrator.creerUtilisateur(request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(gestionCompteKeycloakService, never()).creerIdentite(any(), any(), any(), any());
    }

    @Test
    void testDageMinistereInactif() {
        request.setRole(RoleUtilisateur.DAGE);
        request.setMinistereId(10L);
        Ministere m = new Ministere();
        m.setActif(false);
        when(ministereRepository.findById(10L)).thenReturn(Optional.of(m));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orchestrator.creerUtilisateur(request));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
    }

    @Test
    void testGestionnaireStructureAbsente() {
        request.setRole(RoleUtilisateur.GESTIONNAIRE);
        request.setStructureId(100L);
        request.setCohorteId(200L);
        when(structureRepository.findById(100L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orchestrator.creerUtilisateur(request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testGestionnaireStructureInactive() {
        request.setRole(RoleUtilisateur.GESTIONNAIRE);
        request.setStructureId(100L);
        request.setCohorteId(200L);
        Structure s = new Structure();
        s.setActif(false);
        when(structureRepository.findById(100L)).thenReturn(Optional.of(s));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orchestrator.creerUtilisateur(request));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
    }

    @Test
    void testGestionnaireStructureSansMinistereV2() {
        request.setRole(RoleUtilisateur.GESTIONNAIRE);
        request.setStructureId(100L);
        request.setCohorteId(200L);
        Structure s = new Structure();
        s.setActif(true);
        s.setMinistereV2(null);
        when(structureRepository.findById(100L)).thenReturn(Optional.of(s));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orchestrator.creerUtilisateur(request));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
    }

    @Test
    void testGestionnaireCohorteAbsente() {
        request.setRole(RoleUtilisateur.GESTIONNAIRE);
        request.setStructureId(100L);
        request.setCohorteId(200L);
        Structure s = new Structure();
        s.setActif(true);
        s.setMinistereV2(new Ministere());
        when(structureRepository.findById(100L)).thenReturn(Optional.of(s));
        when(cohorteRepository.findById(200L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orchestrator.creerUtilisateur(request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testGestionnaireCohorteInactive() {
        request.setRole(RoleUtilisateur.GESTIONNAIRE);
        request.setStructureId(100L);
        request.setCohorteId(200L);
        Structure s = new Structure();
        s.setActif(true);
        s.setMinistereV2(new Ministere());
        when(structureRepository.findById(100L)).thenReturn(Optional.of(s));
        Cohorte c = new Cohorte();
        c.setActif(false);
        when(cohorteRepository.findById(200L)).thenReturn(Optional.of(c));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orchestrator.creerUtilisateur(request));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
    }

    @Test
    void testRoleAdminInvalide_MinisterePresent() {
        request.setRole(RoleUtilisateur.ADMIN);
        request.setMinistereId(1L);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orchestrator.creerUtilisateur(request));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
    }

    @Test
    void testRoleDageInvalide_StructurePresente() {
        request.setRole(RoleUtilisateur.DAGE);
        request.setMinistereId(1L);
        request.setStructureId(10L);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orchestrator.creerUtilisateur(request));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
    }

    @Test
    void testEmailNormalization() {
        request.setRole(RoleUtilisateur.ADMIN);
        request.setEmail("  Test.User@AEME.SN  ");
        request.setEmailSecondaire(" Sec.User@AEME.SN ");

        UUID keycloakId = UUID.randomUUID();
        when(gestionCompteKeycloakService.creerIdentite(eq("test.user@aeme.sn"), any(), any(), any())).thenReturn(keycloakId);

        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setId(1L);
        when(sauvegardeProfilService.sauvegarder(any(), any(), any(), any(), any())).thenReturn(profil);

        orchestrator.creerUtilisateur(request);

        assertEquals("test.user@aeme.sn", request.getEmail());
        assertEquals("sec.user@aeme.sn", request.getEmailSecondaire());
        verify(profilUtilisateurRepository).findByEmailIgnoreCase("test.user@aeme.sn");
        verify(gestionCompteKeycloakService).emailExiste("test.user@aeme.sn");
    }

    @Test
    void testChatFailureDoesNotRollback() {
        request.setRole(RoleUtilisateur.ADMIN);
        UUID keycloakId = UUID.randomUUID();
        when(gestionCompteKeycloakService.creerIdentite(any(), any(), any(), eq("admin"))).thenReturn(keycloakId);

        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setId(1L);
        when(sauvegardeProfilService.sauvegarder(any(), any(), any(), any(), any())).thenReturn(profil);

        doThrow(new RuntimeException("Simulated chat DB failure"))
                .when(chatUserMembershipService).syncUserMemberships(profil);

        CreationUtilisateurResponse response = orchestrator.creerUtilisateur(request);

        assertTrue(response.isInvitationEnvoyee());
        verify(gestionCompteKeycloakService).envoyerActionsInitiales(keycloakId);
        verify(gestionCompteKeycloakService, never()).supprimerIdentite(keycloakId);
    }
}
