package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfilUtilisateurCourantServiceTest {

    @Mock
    private ProfilUtilisateurRepository profilUtilisateurRepository;

    @InjectMocks
    private ProfilUtilisateurCourantService profilUtilisateurCourantService;

    private JwtAuthenticationToken authentication;
    private Jwt jwt;
    private UUID validUuid;

    @BeforeEach
    void setUp() {
        validUuid = UUID.randomUUID();
    }

    private void setupAuthentication(String subject) {
        jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(subject);
        authentication = mock(JwtAuthenticationToken.class);
        when(authentication.getToken()).thenReturn(jwt);
    }

    @Test
    void testObtenirProfilCourant_Success() {
        setupAuthentication(validUuid.toString());
        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setKeycloakId(validUuid);
        profil.setActif(true);

        when(profilUtilisateurRepository.findByKeycloakId(validUuid)).thenReturn(Optional.of(profil));

        ProfilUtilisateur result = profilUtilisateurCourantService.obtenirProfilCourant(authentication);

        assertNotNull(result);
        assertEquals(validUuid, result.getKeycloakId());
        verify(profilUtilisateurRepository, times(1)).findByKeycloakId(validUuid);
    }

    @Test
    void testObtenirProfilCourant_NullAuthentication() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            profilUtilisateurCourantService.obtenirProfilCourant(null);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verifyNoInteractions(profilUtilisateurRepository);
    }

    @Test
    void testObtenirProfilCourant_NullSubject() {
        setupAuthentication(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verifyNoInteractions(profilUtilisateurRepository);
    }

    @Test
    void testObtenirProfilCourant_BlankSubject() {
        setupAuthentication("   ");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verifyNoInteractions(profilUtilisateurRepository);
    }

    @Test
    void testObtenirProfilCourant_InvalidUuid() {
        setupAuthentication("invalid-uuid");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verifyNoInteractions(profilUtilisateurRepository);
    }

    @Test
    void testObtenirProfilCourant_ProfilAbsent() {
        setupAuthentication(validUuid.toString());
        when(profilUtilisateurRepository.findByKeycloakId(validUuid)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(profilUtilisateurRepository, times(1)).findByKeycloakId(validUuid);
    }

    @Test
    void testObtenirProfilCourant_ProfilInactif() {
        setupAuthentication(validUuid.toString());
        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setKeycloakId(validUuid);
        profil.setActif(false);

        when(profilUtilisateurRepository.findByKeycloakId(validUuid)).thenReturn(Optional.of(profil));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(profilUtilisateurRepository, times(1)).findByKeycloakId(validUuid);
    }
}
