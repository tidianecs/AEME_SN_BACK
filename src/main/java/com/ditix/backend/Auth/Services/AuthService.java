package com.ditix.backend.Auth.Services;

import com.ditix.backend.Core.EmailService;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final Keycloak keycloak;
    private final EmailService emailService;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public AuthService(Keycloak keycloak, EmailService emailService) {
        this.keycloak = keycloak;
        this.emailService = emailService;
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
        user.setEmailVerified(true);
        user.setRequiredActions(List.of("UPDATE_PASSWORD"));

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

        String tempPassword = generateTempPassword();

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(tempPassword);
        credential.setTemporary(true);
        keycloak.realm(realm).users().get(userId).resetPassword(credential);

        try {
            emailService.sendInvitationEmail(email, firstName, tempPassword, frontendUrl);
        } catch (Exception e) {
            System.err.println("Erreur envoi email invitation : " + e.getMessage());
        }
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$%";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
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
        result.put("cohorte",             getAttr(user, "cohorte"));
        result.put("dateInstallation",    getAttr(user, "dateInstallation"));
        result.put("dateFormation",       getAttr(user, "dateFormation"));
        result.put("derniereMiseANiveau", getAttr(user, "derniereMiseANiveau"));
        result.put("nombreSitesGeres",    getAttr(user, "nombreSitesGeres"));
        result.put("typeBatiment",        getAttr(user, "typeBatiment"));
        result.put("ministere",           getAttr(user, "ministere"));
        result.put("region",              getAttr(user, "region"));
        result.put("zone",                getAttr(user, "zone"));
        result.put("categorieZone",       getAttr(user, "categorieZone"));
        result.put("categorie",           getAttr(user, "categorie"));
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
        profile.put("genre",               getAttr(user, "genre"));
        profile.put("dateNaissance",       getAttr(user, "dateNaissance"));
        profile.put("contact1",            getAttr(user, "contact1"));
        profile.put("contact2",            getAttr(user, "contact2"));
        profile.put("emailSecondaire",     getAttr(user, "emailSecondaire"));
        profile.put("departement",         getAttr(user, "departement"));
        profile.put("posteOccupe",         getAttr(user, "posteOccupe"));
        profile.put("dateNomination",      getAttr(user, "dateNomination"));
        profile.put("cohorte",             getAttr(user, "cohorte"));
        profile.put("dateInstallation",    getAttr(user, "dateInstallation"));
        profile.put("dateFormation",       getAttr(user, "dateFormation"));
        profile.put("derniereMiseANiveau", getAttr(user, "derniereMiseANiveau"));
        profile.put("nombreSitesGeres",    getAttr(user, "nombreSitesGeres"));
        profile.put("typeBatiment",        getAttr(user, "typeBatiment"));
        profile.put("ministere",           getAttr(user, "ministere"));
        profile.put("region",              getAttr(user, "region"));
        profile.put("zone",                getAttr(user, "zone"));
        profile.put("categorieZone",       getAttr(user, "categorieZone"));
        profile.put("categorie",           getAttr(user, "categorie"));
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
            "cohorte", "dateInstallation", "dateFormation",
            "derniereMiseANiveau", "nombreSitesGeres", "typeBatiment",
            "ministere", "region", "zone", "categorieZone", "categorie"
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

    public void updateMyMembership(String userId, String membershipService) {
        updateMembershipService(userId, membershipService);
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

    public Map<String, Object> getAllUsersPaginated(int first, int max, String search) {
        List<UserRepresentation> users;
        int total;

        if (search != null && !search.isBlank()) {
            users = keycloak.realm(realm).users().search(search, first, max);
            total = keycloak.realm(realm).users().count(search);
        } else {
            users = keycloak.realm(realm).users().list(first, max);
            total = keycloak.realm(realm).users().count();
        }

        List<Map<String, String>> result = users.stream()
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

        Map<String, Object> response = new HashMap<>();
        response.put("users", result);
        response.put("total", total);
        response.put("first", first);
        response.put("max",   max);
        return response;
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

    public void deleteUser(String userId) {
        keycloak.realm(realm).users().get(userId).remove();
    }

    private String getAttr(UserRepresentation user, String key) {
        if (user.getAttributes() == null) return "";
        List<String> vals = user.getAttributes().get(key);
        return (vals != null && !vals.isEmpty()) ? vals.get(0) : "";
    }

    public List<Map<String, Object>> getStatsByRegion() {
        // Keycloak ne supporte pas Integer.MAX_VALUE — utilise 10000
        List<UserRepresentation> allUsers = keycloak.realm(realm).users().list(0, 10000);

        Map<String, List<UserRepresentation>> byRegion = allUsers.stream()
            .filter(u -> !getAttr(u, "region").isBlank())
            .collect(Collectors.groupingBy(u -> getAttr(u, "region")));

        return byRegion.entrySet().stream()
            .map(entry -> {
                String region = entry.getKey();
                List<UserRepresentation> users = entry.getValue();

                long gestionnaires = users.size();
                long structures = users.stream()
                    .map(u -> getAttr(u, "membershipService"))
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .count();

                Map<String, Object> stat = new HashMap<>();
                stat.put("region",        region);
                stat.put("gestionnaires", gestionnaires);
                stat.put("structures",    structures);
                return stat;
            })
            .sorted(Comparator.comparing(m -> (String) m.get("region")))
            .collect(Collectors.toList());
    }
}