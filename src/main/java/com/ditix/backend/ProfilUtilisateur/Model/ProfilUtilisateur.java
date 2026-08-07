package com.ditix.backend.ProfilUtilisateur.Model;

import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Cohorte.Model.Cohorte;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "profils_utilisateurs")
public class ProfilUtilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_id", nullable = false, unique = true)
    private UUID keycloakId;

    @Column(nullable = false, length = 150)
    private String prenom;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "telephone_principal", length = 30)
    private String telephonePrincipal;

    @Column(name = "telephone_secondaire", length = 30)
    private String telephoneSecondaire;

    @Column(name = "email_secondaire", length = 255)
    private String emailSecondaire;

    @Column(length = 20)
    private String genre;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "departement_administratif", length = 150)
    private String departementAdministratif;

    @Column(name = "poste_occupe", length = 150)
    private String posteOccupe;

    @Column(name = "date_nomination")
    private LocalDate dateNomination;

    @Column(name = "date_installation")
    private LocalDate dateInstallation;

    @Column(name = "date_formation")
    private LocalDate dateFormation;

    @Column(name = "derniere_mise_a_niveau")
    private LocalDate derniereMiseANiveau;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoleUtilisateur role;

    @ManyToOne
    @JoinColumn(name = "ministere_id")
    private Ministere ministere;

    @ManyToOne
    @JoinColumn(name = "structure_id")
    private Structure structure;

    @ManyToOne
    @JoinColumn(name = "cohorte_id")
    private Cohorte cohorte;

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private ZonedDateTime creeLe;

    @Column(name = "modifie_le", nullable = false)
    private ZonedDateTime modifieLe;

    @PrePersist
    protected void onCreate() {
        this.creeLe = ZonedDateTime.now();
        this.modifieLe = this.creeLe;
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifieLe = ZonedDateTime.now();
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getKeycloakId() { return keycloakId; }
    public void setKeycloakId(UUID keycloakId) { this.keycloakId = keycloakId; }

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

    public Ministere getMinistere() { return ministere; }
    public void setMinistere(Ministere ministere) { this.ministere = ministere; }

    public Structure getStructure() { return structure; }
    public void setStructure(Structure structure) { this.structure = structure; }

    public Cohorte getCohorte() { return cohorte; }
    public void setCohorte(Cohorte cohorte) { this.cohorte = cohorte; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public ZonedDateTime getCreeLe() { return creeLe; }
    public void setCreeLe(ZonedDateTime creeLe) { this.creeLe = creeLe; }

    public ZonedDateTime getModifieLe() { return modifieLe; }
    public void setModifieLe(ZonedDateTime modifieLe) { this.modifieLe = modifieLe; }
}
