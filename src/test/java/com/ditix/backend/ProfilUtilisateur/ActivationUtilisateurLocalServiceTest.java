package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.Chat.Services.ChatUserMembershipService;
import com.ditix.backend.ProfilUtilisateur.DTO.ActivationUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.ProfilUtilisateur.Services.ActivationUtilisateurLocalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActivationUtilisateurLocalServiceTest {

    @Mock
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    @Mock
    private ChatUserMembershipService chatUserMembershipService;

    @InjectMocks
    private ActivationUtilisateurLocalService localService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processLocalActivation_lastAdmin_shouldThrow422() {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(false);

        ProfilUtilisateur target = new ProfilUtilisateur();
        target.setId(1L);
        target.setRole(RoleUtilisateur.ADMIN);
        target.setActif(true);

        when(profilUtilisateurRepository.findById(1L)).thenReturn(Optional.of(target));
        when(profilUtilisateurRepository.findAllAdminsForUpdate()).thenReturn(List.of(target));
        when(profilUtilisateurRepository.findAll()).thenReturn(List.of(target)); // active admin count = 1

        assertThrows(ResponseStatusException.class, () -> localService.processLocalActivation(1L, request));
    }

    @Test
    void processLocalActivation_deactivateDage_shouldDeactivateManagedMemberships() {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(false);

        ProfilUtilisateur target = new ProfilUtilisateur();
        target.setId(2L);
        target.setRole(RoleUtilisateur.DAGE);
        target.setActif(true);
        target.setKeycloakId(UUID.randomUUID());

        when(profilUtilisateurRepository.findById(2L)).thenReturn(Optional.of(target));

        localService.processLocalActivation(2L, request);

        verify(profilUtilisateurRepository, times(1)).save(target);
        verify(chatUserMembershipService, times(1)).deactivateManagedMemberships(target.getKeycloakId().toString());
        verify(chatUserMembershipService, never()).syncUserMemberships(any());
    }

    @Test
    void processLocalActivation_reactivateGestionnaire_shouldSyncManagedMemberships() {
        ActivationUtilisateurRequest request = new ActivationUtilisateurRequest();
        request.setActif(true);

        ProfilUtilisateur target = new ProfilUtilisateur();
        target.setId(3L);
        target.setRole(RoleUtilisateur.GESTIONNAIRE);
        target.setActif(false);
        target.setKeycloakId(UUID.randomUUID());

        when(profilUtilisateurRepository.findById(3L)).thenReturn(Optional.of(target));

        localService.processLocalActivation(3L, request);

        verify(profilUtilisateurRepository, times(1)).save(target);
        verify(chatUserMembershipService, never()).deactivateManagedMemberships(anyString());
        verify(chatUserMembershipService, times(1)).syncUserMemberships(target);
    }
}
