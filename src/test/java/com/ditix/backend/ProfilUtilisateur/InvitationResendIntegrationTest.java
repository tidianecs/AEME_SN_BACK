package com.ditix.backend.ProfilUtilisateur;



import com.ditix.backend.ProfilUtilisateur.Services.GestionCompteKeycloakService;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import org.keycloak.admin.client.Keycloak;

import org.keycloak.admin.client.resource.RealmResource;

import org.keycloak.admin.client.resource.UserResource;

import org.keycloak.admin.client.resource.UsersResource;

import org.keycloak.representations.idm.UserRepresentation;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.MockitoAnnotations;

import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.web.server.ResponseStatusException;



import java.util.ArrayList;

import java.util.Arrays;

import java.util.List;

import java.util.UUID;



import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;



public class InvitationResendIntegrationTest {



    @Mock

    private Keycloak keycloak;



    @Mock

    private RealmResource realmResource;



    @Mock

    private UsersResource usersResource;



    @Mock

    private UserResource userResource;



    @InjectMocks

    private GestionCompteKeycloakService gestionCompteKeycloakService;



    private final UUID testKeycloakId = UUID.randomUUID();



    @BeforeEach

    void setUp() {

        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(gestionCompteKeycloakService, "realm", "test-realm");



        when(keycloak.realm("test-realm")).thenReturn(realmResource);

        when(realmResource.users()).thenReturn(usersResource);

        when(usersResource.get(testKeycloakId.toString())).thenReturn(userResource);

    }



    @Test
    void testIsInvitationPending_BothActions() {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setEmailVerified(false);
        when(userResource.toRepresentation()).thenReturn(userRep);
        when(userResource.credentials()).thenReturn(new java.util.ArrayList<>());

        assertTrue(gestionCompteKeycloakService.isInvitationPending(testKeycloakId));
    }

    @Test
    void testIsInvitationPending_NoAction() {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setEmailVerified(true);
        when(userResource.toRepresentation()).thenReturn(userRep);

        org.keycloak.representations.idm.CredentialRepresentation pwd = new org.keycloak.representations.idm.CredentialRepresentation();
        pwd.setType(org.keycloak.representations.idm.CredentialRepresentation.PASSWORD);
        when(userResource.credentials()).thenReturn(java.util.Collections.singletonList(pwd));

        assertFalse(gestionCompteKeycloakService.isInvitationPending(testKeycloakId));
    }



    @Test

    void testResendInvitation_BothActions() {

        UserRepresentation userRep = new UserRepresentation();

        userRep.setRequiredActions(Arrays.asList("VERIFY_EMAIL", "UPDATE_PASSWORD"));

        when(userResource.toRepresentation()).thenReturn(userRep);



        gestionCompteKeycloakService.resendInvitation(testKeycloakId);



        verify(userResource).executeActionsEmail(

                eq("frontend-aeme"),

                eq("https://aeme-energymanager-front.vercel.app/login"),

                eq(1209600),

                eq(Arrays.asList("VERIFY_EMAIL", "UPDATE_PASSWORD"))

        );

    }



    @Test

    void testResendInvitation_OneAction() {

        UserRepresentation userRep = new UserRepresentation();

        userRep.setRequiredActions(Arrays.asList("VERIFY_EMAIL"));

        when(userResource.toRepresentation()).thenReturn(userRep);



        gestionCompteKeycloakService.resendInvitation(testKeycloakId);



        verify(userResource).executeActionsEmail(

                eq("frontend-aeme"),

                eq("https://aeme-energymanager-front.vercel.app/login"),

                eq(1209600),

                eq(Arrays.asList("VERIFY_EMAIL"))

        );

    }



    @Test

    void testResendInvitation_AlreadyActivated() {

        UserRepresentation userRep = new UserRepresentation();

        userRep.setRequiredActions(new ArrayList<>());

        when(userResource.toRepresentation()).thenReturn(userRep);



        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {

            gestionCompteKeycloakService.resendInvitation(testKeycloakId);

        });



        assertEquals(409, exception.getStatusCode().value());

        assertTrue(exception.getReason().contains("L'activation du compte est déjà terminée"));



        verify(userResource, never()).executeActionsEmail(any(), any(), anyInt(), any());

    }



    @Test

    void testResendInvitation_MissingIdentity() {

        when(userResource.toRepresentation()).thenThrow(new jakarta.ws.rs.NotFoundException());



        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {

            gestionCompteKeycloakService.resendInvitation(testKeycloakId);

        });



        assertEquals(409, exception.getStatusCode().value());

        assertTrue(exception.getReason().contains("L'identité Keycloak de cet utilisateur est introuvable."));

    }




    @Test
    void testEnvoyerActionsInitiales() {
        gestionCompteKeycloakService.envoyerActionsInitiales(testKeycloakId);

        verify(userResource).executeActionsEmail(
                eq("frontend-aeme"),
                eq("https://aeme-energymanager-front.vercel.app/login"),
                eq(1209600),
                eq(Arrays.asList("VERIFY_EMAIL", "UPDATE_PASSWORD"))
        );
    }
}
