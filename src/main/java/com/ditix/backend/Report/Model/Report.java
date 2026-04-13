package com.ditix.backend.Report.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus reportStatus;

    @Column(nullable = false)
    private String createdByUserId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Section 1
    private LocalDateTime reportDate;

    // Section 2
    private String nomGestionnaire;
    private String serviceAppartenance;
    private Integer nombreBatiments;
    private String numeroPoliceSenelec;

    // Section 3
    @Column(columnDefinition = "TEXT")
    private String campagnesCommunication;

    @Column(columnDefinition = "TEXT")
    private String autreCampagnePrecision;

    private Boolean guidePartageCommande;
    private Boolean guidePartagePerformance;
    private Boolean procedureResiliation;
    private Boolean modificationPuissance;

    // Pièce justificative modification puissance
    private String pieceJustificativeModificationPath;
    private String pieceJustificativeModificationName;

    private Boolean consommationsNullesIdentifiees;

    // Action consommations nulles
    private String actionConsommationsNulles;

    private Boolean estimationsRecensees;

    // Action estimations
    private String actionEstimations;

    private Boolean batteriesCondensateursInstallees;

    // Nombre batteries
    private Integer nombreBatteriesCondensateurs;

    private Boolean cadastreEnergetiqueRealise;
    private Boolean indexTransmis;

    // Index transmis détails
    private LocalDateTime dateIndexTransmis;
    private String indexConsommation;

    private Boolean plateformeDigitale;

    // Suivi plateforme digitale
    private Boolean suiviPlateformeDigitale;

    @Column(columnDefinition = "TEXT")
    private String autresActivites;

    @Column(columnDefinition = "TEXT")
    private String autreActivitePrecision;

    @Column(columnDefinition = "TEXT")
    private String contraintes;

    @Column(columnDefinition = "TEXT")
    private String recommandations;

    // Section 4
    private String illustrationsPath;
    private String illustrationsName;
    private String autresDocumentsPath;
    private String autresDocumentsName;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.reportStatus == null) {
            this.reportStatus = ReportStatus.SUBMITTED;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Report report = new Report();

        public Builder reportDate(LocalDateTime date) { report.reportDate = date; return this; }
        public Builder createdByUserId(String userId) { report.createdByUserId = userId; return this; }
        public Builder nomGestionnaire(String nom) { report.nomGestionnaire = nom; return this; }
        public Builder serviceAppartenance(String service) { report.serviceAppartenance = service; return this; }
        public Builder nombreBatiments(Integer nombre) { report.nombreBatiments = nombre; return this; }
        public Builder numeroPoliceSenelec(String numero) { report.numeroPoliceSenelec = numero; return this; }
        public Builder campagnesCommunication(String json) { report.campagnesCommunication = json; return this; }
        public Builder autreCampagnePrecision(String val) { report.autreCampagnePrecision = val; return this; }
        public Builder guidePartageCommande(Boolean val) { report.guidePartageCommande = val; return this; }
        public Builder guidePartagePerformance(Boolean val) { report.guidePartagePerformance = val; return this; }
        public Builder procedureResiliation(Boolean val) { report.procedureResiliation = val; return this; }
        public Builder modificationPuissance(Boolean val) { report.modificationPuissance = val; return this; }
        public Builder pieceJustificativeModificationPath(String path) { report.pieceJustificativeModificationPath = path; return this; }
        public Builder pieceJustificativeModificationName(String name) { report.pieceJustificativeModificationName = name; return this; }
        public Builder consommationsNullesIdentifiees(Boolean val) { report.consommationsNullesIdentifiees = val; return this; }
        public Builder actionConsommationsNulles(String val) { report.actionConsommationsNulles = val; return this; }
        public Builder estimationsRecensees(Boolean val) { report.estimationsRecensees = val; return this; }
        public Builder actionEstimations(String val) { report.actionEstimations = val; return this; }
        public Builder batteriesCondensateursInstallees(Boolean val) { report.batteriesCondensateursInstallees = val; return this; }
        public Builder nombreBatteriesCondensateurs(Integer val) { report.nombreBatteriesCondensateurs = val; return this; }
        public Builder cadastreEnergetiqueRealise(Boolean val) { report.cadastreEnergetiqueRealise = val; return this; }
        public Builder indexTransmis(Boolean val) { report.indexTransmis = val; return this; }
        public Builder dateIndexTransmis(LocalDateTime val) { report.dateIndexTransmis = val; return this; }
        public Builder indexConsommation(String val) { report.indexConsommation = val; return this; }
        public Builder plateformeDigitale(Boolean val) { report.plateformeDigitale = val; return this; }
        public Builder suiviPlateformeDigitale(Boolean val) { report.suiviPlateformeDigitale = val; return this; }
        public Builder autresActivites(String json) { report.autresActivites = json; return this; }
        public Builder autreActivitePrecision(String val) { report.autreActivitePrecision = val; return this; }
        public Builder contraintes(String contraintes) { report.contraintes = contraintes; return this; }
        public Builder recommandations(String recommandations) { report.recommandations = recommandations; return this; }
        public Builder illustrationsPath(String path) { report.illustrationsPath = path; return this; }
        public Builder illustrationsName(String name) { report.illustrationsName = name; return this; }
        public Builder autresDocumentsPath(String path) { report.autresDocumentsPath = path; return this; }
        public Builder autresDocumentsName(String name) { report.autresDocumentsName = name; return this; }
        public Report build() { return report; }
    }

    // Getters & Setters
    public Long getId() { return id; }
    public ReportStatus getReportStatus() { return reportStatus; }
    public void setReportStatus(ReportStatus reportStatus) { this.reportStatus = reportStatus; }
    public String getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(String createdByUserId) { this.createdByUserId = createdByUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }
    public String getNomGestionnaire() { return nomGestionnaire; }
    public void setNomGestionnaire(String nomGestionnaire) { this.nomGestionnaire = nomGestionnaire; }
    public String getServiceAppartenance() { return serviceAppartenance; }
    public void setServiceAppartenance(String serviceAppartenance) { this.serviceAppartenance = serviceAppartenance; }
    public Integer getNombreBatiments() { return nombreBatiments; }
    public void setNombreBatiments(Integer nombreBatiments) { this.nombreBatiments = nombreBatiments; }
    public String getNumeroPoliceSenelec() { return numeroPoliceSenelec; }
    public void setNumeroPoliceSenelec(String numeroPoliceSenelec) { this.numeroPoliceSenelec = numeroPoliceSenelec; }
    public String getCampagnesCommunication() { return campagnesCommunication; }
    public void setCampagnesCommunication(String campagnesCommunication) { this.campagnesCommunication = campagnesCommunication; }
    public String getAutreCampagnePrecision() { return autreCampagnePrecision; }
    public void setAutreCampagnePrecision(String autreCampagnePrecision) { this.autreCampagnePrecision = autreCampagnePrecision; }
    public Boolean getGuidePartageCommande() { return guidePartageCommande; }
    public void setGuidePartageCommande(Boolean guidePartageCommande) { this.guidePartageCommande = guidePartageCommande; }
    public Boolean getGuidePartagePerformance() { return guidePartagePerformance; }
    public void setGuidePartagePerformance(Boolean guidePartagePerformance) { this.guidePartagePerformance = guidePartagePerformance; }
    public Boolean getProcedureResiliation() { return procedureResiliation; }
    public void setProcedureResiliation(Boolean procedureResiliation) { this.procedureResiliation = procedureResiliation; }
    public Boolean getModificationPuissance() { return modificationPuissance; }
    public void setModificationPuissance(Boolean modificationPuissance) { this.modificationPuissance = modificationPuissance; }
    public String getPieceJustificativeModificationPath() { return pieceJustificativeModificationPath; }
    public void setPieceJustificativeModificationPath(String path) { this.pieceJustificativeModificationPath = path; }
    public String getPieceJustificativeModificationName() { return pieceJustificativeModificationName; }
    public void setPieceJustificativeModificationName(String name) { this.pieceJustificativeModificationName = name; }
    public Boolean getConsommationsNullesIdentifiees() { return consommationsNullesIdentifiees; }
    public void setConsommationsNullesIdentifiees(Boolean v) { this.consommationsNullesIdentifiees = v; }
    public String getActionConsommationsNulles() { return actionConsommationsNulles; }
    public void setActionConsommationsNulles(String actionConsommationsNulles) { this.actionConsommationsNulles = actionConsommationsNulles; }
    public Boolean getEstimationsRecensees() { return estimationsRecensees; }
    public void setEstimationsRecensees(Boolean estimationsRecensees) { this.estimationsRecensees = estimationsRecensees; }
    public String getActionEstimations() { return actionEstimations; }
    public void setActionEstimations(String actionEstimations) { this.actionEstimations = actionEstimations; }
    public Boolean getBatteriesCondensateursInstallees() { return batteriesCondensateursInstallees; }
    public void setBatteriesCondensateursInstallees(Boolean v) { this.batteriesCondensateursInstallees = v; }
    public Integer getNombreBatteriesCondensateurs() { return nombreBatteriesCondensateurs; }
    public void setNombreBatteriesCondensateurs(Integer nombreBatteriesCondensateurs) { this.nombreBatteriesCondensateurs = nombreBatteriesCondensateurs; }
    public Boolean getCadastreEnergetiqueRealise() { return cadastreEnergetiqueRealise; }
    public void setCadastreEnergetiqueRealise(Boolean v) { this.cadastreEnergetiqueRealise = v; }
    public Boolean getIndexTransmis() { return indexTransmis; }
    public void setIndexTransmis(Boolean indexTransmis) { this.indexTransmis = indexTransmis; }
    public LocalDateTime getDateIndexTransmis() { return dateIndexTransmis; }
    public void setDateIndexTransmis(LocalDateTime dateIndexTransmis) { this.dateIndexTransmis = dateIndexTransmis; }
    public String getIndexConsommation() { return indexConsommation; }
    public void setIndexConsommation(String indexConsommation) { this.indexConsommation = indexConsommation; }
    public Boolean getPlateformeDigitale() { return plateformeDigitale; }
    public void setPlateformeDigitale(Boolean plateformeDigitale) { this.plateformeDigitale = plateformeDigitale; }
    public Boolean getSuiviPlateformeDigitale() { return suiviPlateformeDigitale; }
    public void setSuiviPlateformeDigitale(Boolean suiviPlateformeDigitale) { this.suiviPlateformeDigitale = suiviPlateformeDigitale; }
    public String getAutresActivites() { return autresActivites; }
    public void setAutresActivites(String autresActivites) { this.autresActivites = autresActivites; }
    public String getAutreActivitePrecision() { return autreActivitePrecision; }
    public void setAutreActivitePrecision(String autreActivitePrecision) { this.autreActivitePrecision = autreActivitePrecision; }
    public String getContraintes() { return contraintes; }
    public void setContraintes(String contraintes) { this.contraintes = contraintes; }
    public String getRecommandations() { return recommandations; }
    public void setRecommandations(String recommandations) { this.recommandations = recommandations; }
    public String getIllustrationsPath() { return illustrationsPath; }
    public void setIllustrationsPath(String illustrationsPath) { this.illustrationsPath = illustrationsPath; }
    public String getIllustrationsName() { return illustrationsName; }
    public void setIllustrationsName(String illustrationsName) { this.illustrationsName = illustrationsName; }
    public String getAutresDocumentsPath() { return autresDocumentsPath; }
    public void setAutresDocumentsPath(String autresDocumentsPath) { this.autresDocumentsPath = autresDocumentsPath; }
    public String getAutresDocumentsName() { return autresDocumentsName; }
    public void setAutresDocumentsName(String autresDocumentsName) { this.autresDocumentsName = autresDocumentsName; }
}