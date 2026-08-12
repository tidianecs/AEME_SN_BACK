package com.ditix.backend.ProfilUtilisateur.DTO;

import jakarta.validation.constraints.NotNull;

public class ActivationUtilisateurRequest {

    @NotNull(message = "Le champ actif est obligatoire")
    private Boolean actif;

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}
