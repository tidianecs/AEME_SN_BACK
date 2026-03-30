package com.ditix.backend.Auth.Services;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
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

    public void inviteUser(String email, String firstName, String lastName, String role, String membershipService) {
        List<UserRepresentation> existing = keycloak.realm(realm)
                .users()
                .searchByEmail(email, true);
        if (!existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setRequiredActions(List.of("VERIFY_EMAIL", "UPDATE_PASSWORD", "UPDATE_PROFILE"));

        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() != 201) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur lors de la création du compte"
            );
        }

        String userId = response.getLocation().getPath()
                .replaceAll(".*/([^/]+)$", "$1");

        RoleRepresentation roleRep = keycloak.realm(realm)
                .roles()
                .get(role)
                .toRepresentation();
        keycloak.realm(realm).users().get(userId)
                .roles().realmLevel().add(List.of(roleRep));

        // Assigne le membershipService si renseigné
        if (membershipService != null && !membershipService.isBlank()) {
            updateMembershipService(userId, membershipService);
        }

        try {
            keycloak.realm(realm).users().get(userId).sendVerifyEmail();
        } catch (Exception e) {
            System.out.println("Email d'invitation non envoyé : " + e.getMessage());
        }
    }

    public Map<String, String> getUserById(String userId) {
        UserRepresentation user = keycloak
            .realm(realm)
            .users()
            .get(userId)
            .toRepresentation();

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName  = user.getLastName()  != null ? user.getLastName()  : "";
        String membershipService = "";
        if (user.getAttributes() != null && user.getAttributes().containsKey("membershipService")) {
            List<String> vals = user.getAttributes().get("membershipService");
            if (vals != null && !vals.isEmpty()) membershipService = vals.get(0);
        }

        return Map.of(
            "id",                user.getId(),
            "email",             user.getEmail() != null ? user.getEmail() : "",
            "firstName",         firstName,
            "lastName",          lastName,
            "fullName",          (firstName + " " + lastName).trim(),
            "membershipService", membershipService
        );
    }

    public Map<String, Object> getUserProfile(String userId, int score) {
        UserRepresentation user = keycloak
            .realm(realm)
            .users()
            .get(userId)
            .toRepresentation();

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName  = user.getLastName()  != null ? user.getLastName()  : "";
        String membershipService = "";
        if (user.getAttributes() != null && user.getAttributes().containsKey("membershipService")) {
            List<String> vals = user.getAttributes().get("membershipService");
            if (vals != null && !vals.isEmpty()) membershipService = vals.get(0);
        }

        List<String> roles = keycloak.realm(realm).users()
                .get(userId)
                .roles().realmLevel().listEffective()
                .stream()
                .map(RoleRepresentation::getName)
                .filter(r -> r.equals("user") || r.equals("admin"))
                .collect(Collectors.toList());
        String role = roles.contains("admin") ? "admin" :
                      roles.contains("user") ? "user" : "none";

        Map<String, Object> profile = new HashMap<>();
        profile.put("id",                user.getId());
        profile.put("username",          user.getUsername() != null ? user.getUsername() : "");
        profile.put("email",             user.getEmail() != null ? user.getEmail() : "");
        profile.put("firstName",         firstName);
        profile.put("lastName",          lastName);
        profile.put("fullName",          (firstName + " " + lastName).trim());
        profile.put("membershipService", membershipService);
        profile.put("role",              role);
        profile.put("score",             score);
        return profile;
    }

    public void updateMembershipService(String userId, String membershipService) {
        UserRepresentation user = keycloak
            .realm(realm)
            .users()
            .get(userId)
            .toRepresentation();

        Map<String, List<String>> attributes = user.getAttributes();
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put("membershipService", Collections.singletonList(membershipService));
        user.setAttributes(attributes);

        keycloak.realm(realm).users().get(userId).update(user);
    }

    public List<Map<String, String>> getAllUsers() {
        return keycloak.realm(realm).users().list().stream()
            .map(user -> {
                List<String> roles = keycloak.realm(realm).users()
                        .get(user.getId())
                        .roles().realmLevel().listEffective()
                        .stream()
                        .map(RoleRepresentation::getName)
                        .filter(r -> r.equals("user") || r.equals("admin"))
                        .collect(Collectors.toList());

                String role = roles.contains("admin") ? "admin" :
                              roles.contains("user") ? "user" : "none";

                String membershipService = "";
                if (user.getAttributes() != null &&
                    user.getAttributes().containsKey("membershipService")) {
                    List<String> vals = user.getAttributes().get("membershipService");
                    if (vals != null && !vals.isEmpty()) membershipService = vals.get(0);
                }

                return Map.of(
                    "id",                user.getId(),
                    "email",             user.getEmail() != null ? user.getEmail() : "",
                    "firstName",         user.getFirstName() != null ? user.getFirstName() : "",
                    "lastName",          user.getLastName() != null ? user.getLastName() : "",
                    "fullName",          ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
                                        (user.getLastName() != null ? user.getLastName() : "")).trim(),
                    "role",              role,
                    "emailVerified",     String.valueOf(user.isEmailVerified()),
                    "membershipService", membershipService
                );
            })
            .collect(Collectors.toList());
    }

    public void deleteUser(String userId) {
        keycloak.realm(realm).users().get(userId).remove();
    }
}