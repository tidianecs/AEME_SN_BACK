package com.ditix.backend.Auth.Services;

import com.ditix.backend.Auth.DTO.RegisterRequest;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final Keycloak keycloak;

    @Value("${keycloak.admin.realm}")
    private String realm;

    public AuthService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public void inviteUser(String email, String firstName, String lastName, String role) {
        // Vérifie si email déjà utilisé
        List<UserRepresentation> existing = keycloak.realm(realm)
                .users()
                .searchByEmail(email, true);
        if (!existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        // Crée le user sans mot de passe
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(false);
        // Action requise : définir le mot de passe + vérifier l'email
        user.setRequiredActions(List.of("UPDATE_PASSWORD", "VERIFY_EMAIL"));

        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() != 201) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur lors de la création du compte"
            );
        }

        // Récupère l'ID du user créé
        String userId = response.getLocation().getPath()
                .replaceAll(".*/([^/]+)$", "$1");

        // Assigne le rôle (user ou admin)
        RoleRepresentation roleRep = keycloak.realm(realm)
                .roles()
                .get(role)
                .toRepresentation();
        keycloak.realm(realm).users().get(userId)
                .roles().realmLevel().add(List.of(roleRep));

        // Envoie l'email d'invitation
        keycloak.realm(realm).users().get(userId)
                .sendVerifyEmail();
    }

    public Map<String, String> getUserById(String userId) {
        UserRepresentation user = keycloak
            .realm(realm)
            .users()
            .get(userId)
            .toRepresentation();

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName  = user.getLastName()  != null ? user.getLastName()  : "";

        return Map.of(
            "id",        user.getId(),
            "email",     user.getEmail() != null ? user.getEmail() : "",
            "firstName", firstName,
            "lastName",  lastName,
            "fullName",  (firstName + " " + lastName).trim()
        );
    }

    public List<Map<String, String>> getAllUsers() {
        return keycloak.realm(realm).users().list().stream()
            .map(user -> {
                // Récupère les rôles du user
                List<String> roles = keycloak.realm(realm).users()
                        .get(user.getId())
                        .roles().realmLevel().listEffective()
                        .stream()
                        .map(RoleRepresentation::getName)
                        .filter(r -> r.equals("user") || r.equals("admin"))
                        .collect(Collectors.toList());

                String role = roles.contains("admin") ? "admin" : 
                              roles.contains("user") ? "user" : "none";

                return Map.of(
                    "id",        user.getId(),
                    "email",     user.getEmail() != null ? user.getEmail() : "",
                    "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                    "lastName",  user.getLastName() != null ? user.getLastName() : "",
                    "fullName",  ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
                                (user.getLastName() != null ? user.getLastName() : "")).trim(),
                    "role",      role
                );
            })
            .collect(Collectors.toList());
    }

    public void deleteUser(String userId) {
        keycloak.realm(realm).users().get(userId).remove();
    }
}