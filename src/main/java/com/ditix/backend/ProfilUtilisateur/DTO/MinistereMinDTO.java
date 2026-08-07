package com.ditix.backend.ProfilUtilisateur.DTO;

import com.ditix.backend.Ministere.Model.Ministere;

public class MinistereMinDTO {
    private Long id;
    private String code;
    private String nom;
    private String nomCourt;

    public MinistereMinDTO(Ministere ministere) {
        if (ministere != null) {
            this.id = ministere.getId();
            this.code = ministere.getCode();
            this.nom = ministere.getNom();
            this.nomCourt = ministere.getNomCourt();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getNomCourt() { return nomCourt; }
    public void setNomCourt(String nomCourt) { this.nomCourt = nomCourt; }
}
