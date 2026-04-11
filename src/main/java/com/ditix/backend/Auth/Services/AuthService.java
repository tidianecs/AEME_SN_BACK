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

        if (membershipService != null && !membershipService.isBlank()) {
            updateMembershipService(userId, membershipService);
        }

        try {
            keycloak.realm(realm).users().get(userId).sendVerifyEmail();
        } catch (Exception e) {
            System.out.println("Email d'invitation non envoyé : " + e.getMessage());
        }
    }

    public Map<String, Object> getUserById(String userId) {
        UserRepresentation user = keycloak
            .realm(realm)
            .users()
            .get(userId)
            .toRepresentation();

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName  = user.getLastName()  != null ? user.getLastName()  : "";

        Map<String, Object> result = new HashMap<>();
        result.put("id",                  user.getId());
        result.put("email",               user.getEmail() != null ? user.getEmail() : "");
        result.put("firstName",           firstName);
        result.put("lastName",            lastName);
        result.put("fullName",            (firstName + " " + lastName).trim());
        result.put("membershipService",   getAttr(user, "membershipService"));
        result.put("genre",               getAttr(user, "genre"));
        result.put("dateNaissance",       getAttr(user, "dateNaissance"));
        result.put("contact1",            getAttr(user, "contact1"));
        result.put("contact2",            getAttr(user, "contact2"));
        result.put("emailSecondaire",     getAttr(user, "emailSecondaire"));
        result.put("departement",         getAttr(user, "departement"));
        result.put("posteOccupe",         getAttr(user, "posteOccupe"));
        result.put("dateNomination",      getAttr(user, "dateNomination"));
        result.put("cohorte1",            getAttr(user, "cohorte1"));
        result.put("cohorte2",            getAttr(user, "cohorte2"));
        result.put("dateInstallation",    getAttr(user, "dateInstallation"));
        result.put("dateFormation",       getAttr(user, "dateFormation"));
        result.put("derniereMiseANiveau", getAttr(user, "derniereMiseANiveau"));
        result.put("nombreSitesGeres",    getAttr(user, "nombreSitesGeres"));
        result.put("typeBatiment",        getAttr(user, "typeBatiment"));
        return result;
    }

    public Map<String, Object> getUserProfile(String userId, int score) {
        UserRepresentation user = keycloak
            .realm(realm)
            .users()
            .get(userId)
            .toRepresentation();

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName  = user.getLastName()  != null ? user.getLastName()  : "";

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
        profile.put("id",                  user.getId());
        profile.put("username",            user.getUsername() != null ? user.getUsername() : "");
        profile.put("email",               user.getEmail() != null ? user.getEmail() : "");
        profile.put("firstName",           firstName);
        profile.put("lastName",            lastName);
        profile.put("fullName",            (firstName + " " + lastName).trim());
        profile.put("membershipService",   getAttr(user, "membershipService"));
        profile.put("role",                role);
        profile.put("score",               score);
        profile.put("serviceLatitude",     getAttr(user, "serviceLatitude"));
        profile.put("serviceLongitude",    getAttr(user, "serviceLongitude"));
        // Section 2 — Profil professionnel
        profile.put("genre",               getAttr(user, "genre"));
        profile.put("dateNaissance",       getAttr(user, "dateNaissance"));
        profile.put("contact1",            getAttr(user, "contact1"));
        profile.put("contact2",            getAttr(user, "contact2"));
        profile.put("emailSecondaire",     getAttr(user, "emailSecondaire"));
        profile.put("departement",         getAttr(user, "departement"));
        profile.put("posteOccupe",         getAttr(user, "posteOccupe"));
        profile.put("dateNomination",      getAttr(user, "dateNomination"));
        // Section 3 — Parcours de formation
        profile.put("cohorte1",            getAttr(user, "cohorte1"));
        profile.put("cohorte2",            getAttr(user, "cohorte2"));
        profile.put("dateInstallation",    getAttr(user, "dateInstallation"));
        profile.put("dateFormation",       getAttr(user, "dateFormation"));
        profile.put("derniereMiseANiveau", getAttr(user, "derniereMiseANiveau"));
        // Section 4 — Périmètre de gestion
        profile.put("nombreSitesGeres",    getAttr(user, "nombreSitesGeres"));
        profile.put("typeBatiment",        getAttr(user, "typeBatiment"));
        return profile;
    }

    public void updateUserProfile(String userId, Map<String, String> fields) {
        UserRepresentation user = keycloak
            .realm(realm)
            .users()
            .get(userId)
            .toRepresentation();

        Map<String, List<String>> attributes = user.getAttributes();
        if (attributes == null) attributes = new HashMap<>();

        String[] attrKeys = {
            "genre", "dateNaissance", "contact1", "contact2", "emailSecondaire",
            "departement", "posteOccupe", "dateNomination",
            "cohorte1", "cohorte2", "dateInstallation", "dateFormation",
            "derniereMiseANiveau", "nombreSitesGeres", "typeBatiment"
        };

        for (String key : attrKeys) {
            if (fields.containsKey(key) && fields.get(key) != null) {
                attributes.put(key, Collections.singletonList(fields.get(key)));
            }
        }

        if (fields.containsKey("firstName") && fields.get("firstName") != null) {
            user.setFirstName(fields.get("firstName"));
        }
        if (fields.containsKey("lastName") && fields.get("lastName") != null) {
            user.setLastName(fields.get("lastName"));
        }

        user.setAttributes(attributes);
        keycloak.realm(realm).users().get(userId).update(user);
    }

    public void updateMembershipService(String userId, String membershipService) {
        UserRepresentation user = keycloak
            .realm(realm)
            .users()
            .get(userId)
            .toRepresentation();

        Map<String, List<String>> attributes = user.getAttributes();
        if (attributes == null) attributes = new HashMap<>();
        attributes.put("membershipService", Collections.singletonList(membershipService));
        user.setAttributes(attributes);
        keycloak.realm(realm).users().get(userId).update(user);
    }

    public void updateServiceLocation(String userId, String latitude, String longitude) {
        UserRepresentation user = keycloak
            .realm(realm)
            .users()
            .get(userId)
            .toRepresentation();

        Map<String, List<String>> attributes = user.getAttributes();
        if (attributes == null) attributes = new HashMap<>();
        attributes.put("serviceLatitude",  Collections.singletonList(latitude));
        attributes.put("serviceLongitude", Collections.singletonList(longitude));
        user.setAttributes(attributes);
        keycloak.realm(realm).users().get(userId).update(user);
    }

    public List<Map<String, Object>> getAllUsersWithLocation() {
        return keycloak.realm(realm).users().list().stream()
            .filter(user -> user.isEmailVerified())
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

                Map<String, Object> u = new HashMap<>();
                u.put("id",                user.getId());
                u.put("fullName",          ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
                                           (user.getLastName()  != null ? user.getLastName()  : "")).trim());
                u.put("email",             user.getEmail() != null ? user.getEmail() : "");
                u.put("role",              role);
                u.put("membershipService", getAttr(user, "membershipService"));
                u.put("serviceLatitude",   getAttr(user, "serviceLatitude"));
                u.put("serviceLongitude",  getAttr(user, "serviceLongitude"));
                return u;
            })
            .filter(u -> !((String) u.get("membershipService")).isBlank())
            .collect(Collectors.toList());
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

                return Map.of(
                    "id",                user.getId(),
                    "email",             user.getEmail() != null ? user.getEmail() : "",
                    "firstName",         user.getFirstName() != null ? user.getFirstName() : "",
                    "lastName",          user.getLastName()  != null ? user.getLastName()  : "",
                    "fullName",          ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
                                        (user.getLastName()  != null ? user.getLastName()  : "")).trim(),
                    "role",              role,
                    "emailVerified",     String.valueOf(user.isEmailVerified()),
                    "membershipService", getAttr(user, "membershipService")
                );
            })
            .collect(Collectors.toList());
    }

    public void deleteUser(String userId) {
        keycloak.realm(realm).users().get(userId).remove();
    }

    private String getAttr(UserRepresentation user, String key) {
        if (user.getAttributes() == null) return "";
        List<String> vals = user.getAttributes().get(key);
        return (vals != null && !vals.isEmpty()) ? vals.get(0) : "";
    }
}