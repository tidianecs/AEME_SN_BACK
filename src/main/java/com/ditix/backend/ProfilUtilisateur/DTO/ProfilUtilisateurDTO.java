package com.ditix.backend.ProfilUtilisateur.DTO;

import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.Structure.Model.Structure;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class ProfilUtilisateurDTO {
    private Long id;
    private String prenom;
    private String nom;
    private String email;

    private String telephonePrincipal;
    private String telephoneSecondaire;
    private String emailSecondaire;

    private String genre;
    private LocalDate dateNaissance;

    private String departementAdministratif;
    private String posteOccupe;

    private LocalDate dateNomination;
    private LocalDate dateInstallation;
    private LocalDate dateFormation;
    private LocalDate derniereMiseANiveau;

    private RoleUtilisateur role;
    private Boolean actif;

    private MinistereMinDTO ministere;
    private StructureMinDTO structure;
    private CohorteMinDTO cohorte;

    private ZonedDateTime creeLe;
    private ZonedDateTime modifieLe;

    private Integer score;

    public ProfilUtilisateurDTO(ProfilUtilisateur profil) {
        if (profil == null) return;

        this.id = profil.getId();
        this.prenom = profil.getPrenom();
        this.nom = profil.getNom();
        this.email = profil.getEmail();

        this.telephonePrincipal = profil.getTelephonePrincipal();
        this.telephoneSecondaire = profil.getTelephoneSecondaire();
        this.emailSecondaire = profil.getEmailSecondaire();

        this.genre = profil.getGenre();
        this.dateNaissance = profil.getDateNaissance();

        this.departementAdministratif = profil.getDepartementAdministratif();
        this.posteOccupe = profil.getPosteOccupe();

        this.dateNomination = profil.getDateNomination();
        this.dateInstallation = profil.getDateInstallation();
        this.dateFormation = profil.getDateFormation();
        this.derniereMiseANiveau = profil.getDerniereMiseANiveau();

        this.role = profil.getRole();
        this.actif = profil.getActif();

        this.creeLe = profil.getCreeLe();
        this.modifieLe = profil.getModifieLe();

        if (profil.getStructure() != null) {
            this.structure = new StructureMinDTO(profil.getStructure());
        }

        if (profil.getCohorte() != null) {
            this.cohorte = new CohorteMinDTO(profil.getCohorte());
        }

        // Logic for Ministere:
        // For Gestionnaire, ministere can be derived from structure.ministereV2 if profil.ministere is null
        if (profil.getMinistere() != null) {
            this.ministere = new MinistereMinDTO(profil.getMinistere());
        } else if (profil.getStructure() != null && profil.getStructure().getMinistereV2() != null) {
            this.ministere = new MinistereMinDTO(profil.getStructure().getMinistereV2());
        }
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephonePrincipal() { return telephonePrincipal; }
    public void setTelephonePrincipal(String telephonePrincipal) { this.telephonePrincipal = telephonePrincipal; }

    public String getTelephoneSecondaire() { return telephoneSecondaire; }
    public void setTelephoneSecondaire(String telephoneSecondaire) { this.telephoneSecondaire = telephoneSecondaire; }

    public String getEmailSecondaire() { return emailSecondaire; }
    public void setEmailSecondaire(String emailSecondaire) { this.emailSecondaire = emailSecondaire; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getDepartementAdministratif() { return departementAdministratif; }
    public void setDepartementAdministratif(String departementAdministratif) { this.departementAdministratif = departementAdministratif; }

    public String getPosteOccupe() { return posteOccupe; }
    public void setPosteOccupe(String posteOccupe) { this.posteOccupe = posteOccupe; }

    public LocalDate getDateNomination() { return dateNomination; }
    public void setDateNomination(LocalDate dateNomination) { this.dateNomination = dateNomination; }

    public LocalDate getDateInstallation() { return dateInstallation; }
    public void setDateInstallation(LocalDate dateInstallation) { this.dateInstallation = dateInstallation; }

    public LocalDate getDateFormation() { return dateFormation; }
    public void setDateFormation(LocalDate dateFormation) { this.dateFormation = dateFormation; }

    public LocalDate getDerniereMiseANiveau() { return derniereMiseANiveau; }
    public void setDerniereMiseANiveau(LocalDate derniereMiseANiveau) { this.derniereMiseANiveau = derniereMiseANiveau; }

    public RoleUtilisateur getRole() { return role; }
    public void setRole(RoleUtilisateur role) { this.role = role; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public MinistereMinDTO getMinistere() { return ministere; }
    public void setMinistere(MinistereMinDTO ministere) { this.ministere = ministere; }

    public StructureMinDTO getStructure() { return structure; }
    public void setStructure(StructureMinDTO structure) { this.structure = structure; }

    public CohorteMinDTO getCohorte() { return cohorte; }
    public void setCohorte(CohorteMinDTO cohorte) { this.cohorte = cohorte; }

    public ZonedDateTime getCreeLe() { return creeLe; }
    public void setCreeLe(ZonedDateTime creeLe) { this.creeLe = creeLe; }

    public ZonedDateTime getModifieLe() { return modifieLe; }
    public void setModifieLe(ZonedDateTime modifieLe) { this.modifieLe = modifieLe; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}
