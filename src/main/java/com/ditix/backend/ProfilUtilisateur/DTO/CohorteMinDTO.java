package com.ditix.backend.ProfilUtilisateur.DTO;

import com.ditix.backend.Cohorte.Model.Cohorte;

public class CohorteMinDTO {
    private Long id;
    private String code;
    private String nom;

    public CohorteMinDTO(Cohorte cohorte) {
        if (cohorte != null) {
            this.id = cohorte.getId();
            this.code = cohorte.getCode();
            this.nom = cohorte.getNom();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}
