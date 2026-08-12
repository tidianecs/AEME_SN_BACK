package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.Auth.Services.AuthService;
import com.ditix.backend.ProfilUtilisateur.DTO.ActivationUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.ProfilUtilisateur.Services.ActivationUtilisateurLocalService;
import com.ditix.backend.ProfilUtilisateur.Services.ActivationUtilisateurOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ActivationUtilisateurOrchestratorTest {

    @Mock
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    @Mock
    private AuthService authService;

    @Mock
    private ActivationUtilisateurLocalService activationUtilisateurLocalService;

    @InjectMocks
    private ActivationUtilisateurOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void activerUtilisateur_targetMissing_shouldThrow404() {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        ProfilUtilisateur actor = new ProfilUtilisateur();

        when(profilUtilisateurRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> orchestrator.activerUtilisateur(1L, request, actor));
    }

    @Test
    void activerUtilisateur_selfDeactivation_shouldThrow403() {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        ProfilUtilisateur actor = new ProfilUtilisateur();
        actor.setId(1L);

        ProfilUtilisateur target = new ProfilUtilisateur();
        target.setId(1L);

        when(profilUtilisateurRepository.findById(1L)).thenReturn(Optional.of(target));

        assertThrows(ResponseStatusException.class, () -> orchestrator.activerUtilisateur(1L, request, actor));
    }

    @Test
    void activerUtilisateur_idempotent_shouldReturnWithoutDoingAnything() {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(true);

        ProfilUtilisateur actor = new ProfilUtilisateur();
        actor.setId(1L);

        ProfilUtilisateur target = new ProfilUtilisateur();
        target.setId(2L);
        target.setKeycloakId(UUID.randomUUID());
        target.setActif(true);

        when(profilUtilisateurRepository.findById(2L)).thenReturn(Optional.of(target));

        orchestrator.activerUtilisateur(2L, request, actor);

        verify(authService, never()).setUserEnabled(any(), anyBoolean());
        verify(activationUtilisateurLocalService, never()).processLocalActivation(any(), any());
    }

    @Test
    void activerUtilisateur_keycloakFailure_shouldNotCallLocalService() {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(false);

        ProfilUtilisateur actor = new ProfilUtilisateur();
        actor.setId(1L);

        ProfilUtilisateur target = new ProfilUtilisateur();
        target.setId(2L);
        target.setKeycloakId(UUID.randomUUID());
        target.setActif(true);

        when(profilUtilisateurRepository.findById(2L)).thenReturn(Optional.of(target));
        when(authService.isUserEnabled(anyString())).thenReturn(true);
        doThrow(new RuntimeException("Keycloak error")).when(authService).setUserEnabled(anyString(), anyBoolean());

        assertThrows(RuntimeException.class, () -> orchestrator.activerUtilisateur(2L, request, actor));

        verify(activationUtilisateurLocalService, never()).processLocalActivation(any(), any());
    }

    @Test
    void activerUtilisateur_localFailure_shouldCompensateKeycloak() {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(false);

        ProfilUtilisateur actor = new ProfilUtilisateur();
        actor.setId(1L);

        ProfilUtilisateur target = new ProfilUtilisateur();
        target.setId(2L);
        target.setKeycloakId(UUID.randomUUID());
        target.setActif(true);

        when(profilUtilisateurRepository.findById(2L)).thenReturn(Optional.of(target));
        when(authService.isUserEnabled(anyString())).thenReturn(true); // previous state
        doThrow(new RuntimeException("DB error")).when(activationUtilisateurLocalService).processLocalActivation(any(), any());

        assertThrows(RuntimeException.class, () -> orchestrator.activerUtilisateur(2L, request, actor));

        // verify keycloak was updated to requested state
        verify(authService, times(1)).setUserEnabled(target.getKeycloakId().toString(), false);
        // verify keycloak compensation (restore previous state)
        verify(authService, times(1)).setUserEnabled(target.getKeycloakId().toString(), true);
    }
}
