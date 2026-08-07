package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.ProfilUtilisateur.Controllers.ProfilUtilisateurController;
import com.ditix.backend.ProfilUtilisateur.DTO.ProfilUtilisateurDTO;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.AutorisationMetierService;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurLectureService;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import com.ditix.backend.Ministere.Model.Ministere;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfilUtilisateurControllerTest {

    @Mock
    private ProfilUtilisateurCourantService profilUtilisateurCourantService;

    @Mock
    private AutorisationMetierService autorisationMetierService;

    @Mock
    private ProfilUtilisateurLectureService profilUtilisateurLectureService;

    @Mock
    private StructureRepository structureRepository;

    @InjectMocks
    private ProfilUtilisateurController controller;

    @Mock
    private JwtAuthenticationToken authentication;

    private ProfilUtilisateur adminProfil;
    private ProfilUtilisateur dageProfil;
    private ProfilUtilisateur gestionnaireProfil;

    @BeforeEach
    void setUp() {
        adminProfil = new ProfilUtilisateur();
        adminProfil.setId(1L);
        adminProfil.setRole(RoleUtilisateur.ADMIN);

        dageProfil = new ProfilUtilisateur();
        dageProfil.setId(2L);
        dageProfil.setRole(RoleUtilisateur.DAGE);
        Ministere m = new Ministere();
        m.setId(10L);
        dageProfil.setMinistere(m);

        gestionnaireProfil = new ProfilUtilisateur();
        gestionnaireProfil.setId(3L);
        gestionnaireProfil.setRole(RoleUtilisateur.GESTIONNAIRE);
        Structure s = new Structure();
        s.setId(20L);
        gestionnaireProfil.setStructure(s);
    }

    @Test
    void testGetMe() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(adminProfil);
        ProfilUtilisateurDTO result = controller.getMe(authentication);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testListAdminUsers_AsAdmin() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(adminProfil);
        when(autorisationMetierService.estAdmin(adminProfil)).thenReturn(true);
        when(profilUtilisateurLectureService.listerTous(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<ProfilUtilisateurDTO> result = controller.listAdminUsers(authentication, 0, 20);
        assertNotNull(result);
    }

    @Test
    void testListAdminUsers_AsNotAdmin() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(dageProfil);
        when(autorisationMetierService.estAdmin(dageProfil)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            controller.listAdminUsers(authentication, 0, 20);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void testListDageGestionnaires_AsDage() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(dageProfil);
        when(autorisationMetierService.estDage(dageProfil)).thenReturn(true);
        when(profilUtilisateurLectureService.listerGestionnairesParMinistere(eq(10L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<ProfilUtilisateurDTO> result = controller.listDageGestionnaires(authentication, 0, 20);
        assertNotNull(result);
    }

    @Test
    void testListDageGestionnaires_AsAdmin() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(adminProfil);
        when(autorisationMetierService.estDage(adminProfil)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            controller.listDageGestionnaires(authentication, 0, 20);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void testListStructureGestionnaires_HasAccess() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(gestionnaireProfil);
        Structure s = new Structure();
        s.setId(20L);
        when(structureRepository.findById(20L)).thenReturn(Optional.of(s));
        when(autorisationMetierService.aAccesStructure(gestionnaireProfil, s)).thenReturn(true);
        when(profilUtilisateurLectureService.listerGestionnairesParStructure(eq(20L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<ProfilUtilisateurDTO> result = controller.listStructureGestionnaires(20L, authentication, 0, 20);
        assertNotNull(result);
    }

    @Test
    void testListStructureGestionnaires_NoAccess() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(gestionnaireProfil);
        Structure s = new Structure();
        s.setId(21L);
        when(structureRepository.findById(21L)).thenReturn(Optional.of(s));
        when(autorisationMetierService.aAccesStructure(gestionnaireProfil, s)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            controller.listStructureGestionnaires(21L, authentication, 0, 20);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void testListStructureGestionnaires_NotFound() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(gestionnaireProfil);
        when(structureRepository.findById(21L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            controller.listStructureGestionnaires(21L, authentication, 0, 20);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testPagination_InvalidPage() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(adminProfil);
        when(autorisationMetierService.estAdmin(adminProfil)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            controller.listAdminUsers(authentication, -1, 20);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testPagination_InvalidSizeZero() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(adminProfil);
        when(autorisationMetierService.estAdmin(adminProfil)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            controller.listAdminUsers(authentication, 0, 0);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testPagination_InvalidSizeNegative() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(adminProfil);
        when(autorisationMetierService.estAdmin(adminProfil)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            controller.listAdminUsers(authentication, 0, -1);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testPagination_InvalidSizeTooLarge() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(adminProfil);
        when(autorisationMetierService.estAdmin(adminProfil)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            controller.listAdminUsers(authentication, 0, 101);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testPagination_ValidSmallSize() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(adminProfil);
        when(autorisationMetierService.estAdmin(adminProfil)).thenReturn(true);
        when(profilUtilisateurLectureService.listerTous(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<ProfilUtilisateurDTO> result = controller.listAdminUsers(authentication, 0, 1);
        assertNotNull(result);
    }

    @Test
    void testPagination_ValidLargeSize() {
        when(profilUtilisateurCourantService.obtenirProfilCourant(authentication)).thenReturn(adminProfil);
        when(autorisationMetierService.estAdmin(adminProfil)).thenReturn(true);
        when(profilUtilisateurLectureService.listerTous(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<ProfilUtilisateurDTO> result = controller.listAdminUsers(authentication, 0, 100);
        assertNotNull(result);
    }
}
