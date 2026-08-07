package com.ditix.backend.Ministere.Model;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "ministeres")
public class Ministere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, unique = true, length = 255)
    private String nom;

    @Column(name = "nom_court", length = 100)
    private String nomCourt;

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(name = "cree_le", nullable = false)
    private ZonedDateTime creeLe;

    @Column(name = "modifie_le", nullable = false)
    private ZonedDateTime modifieLe;

    @PrePersist
    protected void onCreate() {
        creeLe = ZonedDateTime.now();
        modifieLe = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifieLe = ZonedDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getNomCourt() { return nomCourt; }
    public void setNomCourt(String nomCourt) { this.nomCourt = nomCourt; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public ZonedDateTime getCreeLe() { return creeLe; }
    public void setCreeLe(ZonedDateTime creeLe) { this.creeLe = creeLe; }

    public ZonedDateTime getModifieLe() { return modifieLe; }
    public void setModifieLe(ZonedDateTime modifieLe) { this.modifieLe = modifieLe; }
}
