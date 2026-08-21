package com.ditix.backend.ProfilUtilisateur;

import com.ditix.backend.ProfilUtilisateur.Services.GestionCompteKeycloakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionCompteKeycloakServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RolesResource rolesResource;
    
    @Mock
    private RoleResource roleResource;
    
    @Mock
    private RoleMappingResource roleMappingResource;
    
    @Mock
    private RoleScopeResource roleScopeResource;

    @Mock
    private Response response;

    @InjectMocks
    private GestionCompteKeycloakService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "realm", "aeme");
    }

    @Test
    void testCreerIdentite() throws Exception {
        when(keycloak.realm("aeme")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        
        Response realResponse = Response.status(201).location(new URI("http://localhost/auth/admin/realms/aeme/users/123e4567-e89b-12d3-a456-426614174000")).build();
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(realResponse);

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("user")).thenReturn(roleResource);
        RoleRepresentation roleRep = new RoleRepresentation();
        roleRep.setName("user");
        when(roleResource.toRepresentation()).thenReturn(roleRep);

        when(usersResource.get("123e4567-e89b-12d3-a456-426614174000")).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        UUID keycloakId = service.creerIdentite("test@aeme.sn", "Prenom", "Nom", "user");

        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), keycloakId);

        ArgumentCaptor<UserRepresentation> userCaptor = ArgumentCaptor.forClass(UserRepresentation.class);
        verify(usersResource).create(userCaptor.capture());

        UserRepresentation capturedUser = userCaptor.getValue();
        assertEquals("test@aeme.sn", capturedUser.getUsername());
        assertEquals("test@aeme.sn", capturedUser.getEmail());
        assertEquals("Prenom", capturedUser.getFirstName());
        assertEquals("Nom", capturedUser.getLastName());
        assertTrue(capturedUser.isEnabled());
        assertFalse(capturedUser.isEmailVerified());
        
        verify(roleScopeResource).add(anyList());
    }

    @Test
    void testEnvoyerActionsInitiales() {
        when(keycloak.realm("aeme")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(anyString())).thenReturn(userResource);

        UUID id = UUID.randomUUID();
        service.envoyerActionsInitiales(id);

        verify(userResource).executeActionsEmail(
            eq("frontend-aeme"),
            eq("https://aeme-energymanager-front.vercel.app/login"),
            eq(1209600),
            eq(List.of("VERIFY_EMAIL", "UPDATE_PASSWORD"))
        );
    }
}
