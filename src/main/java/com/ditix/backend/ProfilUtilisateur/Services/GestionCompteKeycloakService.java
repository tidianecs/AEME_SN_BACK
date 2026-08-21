package com.ditix.backend.ProfilUtilisateur.Services;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Service
public class GestionCompteKeycloakService {
    private final Keycloak keycloak;
    @Value("${keycloak.admin.realm}")
    private String realm;
    private static final int INVITATION_LIFESPAN_SECONDS = 14 * 24 * 60 * 60;
    public GestionCompteKeycloakService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }
    public boolean emailExiste(String email) {
        List<UserRepresentation> existing = keycloak.realm(realm).users().searchByEmail(email, true);
        return !existing.isEmpty();
    }
    public UUID creerIdentite(String email, String prenom, String nom, String roleKeycloak) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(prenom);
        user.setLastName(nom);
        user.setEnabled(true);
        user.setEmailVerified(false);
        String userId = null;
        try (Response response = keycloak.realm(realm).users().create(user)) {
            if (response.getStatus() == 409) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "L'utilisateur existe déjà dans Keycloak");
            }
            if (response.getStatus() != 201) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la création Keycloak");
            }
            userId = CreatedResponseUtil.getCreatedId(response);
        }
        RoleRepresentation roleRep = keycloak.realm(realm).roles().get(roleKeycloak).toRepresentation();
        keycloak.realm(realm).users().get(userId).roles().realmLevel().add(List.of(roleRep));
        return UUID.fromString(userId);
    }
    public void envoyerActionsInitiales(UUID keycloakId) {
        keycloak.realm(realm).users().get(keycloakId.toString()).executeActionsEmail(
            "frontend-aeme",
            "https://aeme-energymanager-front.vercel.app/login",
            INVITATION_LIFESPAN_SECONDS,
            List.of("VERIFY_EMAIL", "UPDATE_PASSWORD")
        );
    }
    public boolean isInvitationPending(UUID keycloakId) {
        try {
            UserRepresentation user = keycloak.realm(realm).users().get(keycloakId.toString()).toRepresentation();
            List<String> actions = user.getRequiredActions();
            if (actions == null) {
                return false;
            }
            return actions.contains("VERIFY_EMAIL") || actions.contains("UPDATE_PASSWORD");
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'identité Keycloak de cet utilisateur est introuvable.");
        }
    }
    public void resendInvitation(UUID keycloakId) {
        try {
            UserRepresentation user = keycloak.realm(realm).users().get(keycloakId.toString()).toRepresentation();
            List<String> actions = user.getRequiredActions();
            if (actions == null || (!actions.contains("VERIFY_EMAIL") && !actions.contains("UPDATE_PASSWORD"))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "L'activation du compte est déjà terminée pour cet utilisateur.");
            }
            List<String> actionsToSend = new ArrayList<>();
            if (actions.contains("VERIFY_EMAIL")) {
                actionsToSend.add("VERIFY_EMAIL");
            }
            if (actions.contains("UPDATE_PASSWORD")) {
                actionsToSend.add("UPDATE_PASSWORD");
            }
            keycloak.realm(realm).users().get(keycloakId.toString()).executeActionsEmail(
                "frontend-aeme",
                "https://aeme-energymanager-front.vercel.app/login",
                INVITATION_LIFESPAN_SECONDS,
                actionsToSend
            );
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'identité Keycloak de cet utilisateur est introuvable.");
        }
    }
    public void supprimerIdentite(UUID keycloakId) {
        keycloak.realm(realm).users().get(keycloakId.toString()).remove();
    }
}
