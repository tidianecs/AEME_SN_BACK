package com.ditix.backend.ProfilUtilisateur.DTO;

public class CreationUtilisateurResponse {
    private ProfilUtilisateurDTO utilisateur;
    private boolean invitationEnvoyee;

    public CreationUtilisateurResponse(ProfilUtilisateurDTO utilisateur, boolean invitationEnvoyee) {
        this.utilisateur = utilisateur;
        this.invitationEnvoyee = invitationEnvoyee;
    }

    public ProfilUtilisateurDTO getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(ProfilUtilisateurDTO utilisateur) {
        this.utilisateur = utilisateur;
    }

    public boolean isInvitationEnvoyee() {
        return invitationEnvoyee;
    }

    public void setInvitationEnvoyee(boolean invitationEnvoyee) {
        this.invitationEnvoyee = invitationEnvoyee;
    }
}
