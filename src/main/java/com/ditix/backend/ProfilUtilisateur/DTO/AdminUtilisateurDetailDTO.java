package com.ditix.backend.ProfilUtilisateur.DTO;



import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;



public class AdminUtilisateurDetailDTO extends ProfilUtilisateurDTO {

    private Boolean invitationPending;



    public AdminUtilisateurDetailDTO(ProfilUtilisateur profil, Boolean invitationPending) {

        super(profil);

        this.invitationPending = invitationPending;

    }



    public Boolean getInvitationPending() {

        return invitationPending;

    }



    public void setInvitationPending(Boolean invitationPending) {

        this.invitationPending = invitationPending;

    }

}
