package com.ditix.backend.ProfilUtilisateur.DTO;

import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreerUtilisateurRequest {
    @NotBlank
    private String prenom;

    @NotBlank
    private String nom;

    @NotBlank
    @Email
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

    @NotNull
    private RoleUtilisateur role;

    private Long ministereId;
    private Long structureId;
    private Long cohorteId;

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

    public Long getMinistereId() { return ministereId; }
    public void setMinistereId(Long ministereId) { this.ministereId = ministereId; }

    public Long getStructureId() { return structureId; }
    public void setStructureId(Long structureId) { this.structureId = structureId; }

    public Long getCohorteId() { return cohorteId; }
    public void setCohorteId(Long cohorteId) { this.cohorteId = cohorteId; }
}
