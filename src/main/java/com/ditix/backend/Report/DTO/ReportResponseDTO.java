package com.ditix.backend.Report.DTO;

import java.time.LocalDateTime;
import com.ditix.backend.Report.Model.Report;

public class ReportResponseDTO {

    private Long id;
    private String reportStatus;
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
    private String campagnesCommunication;
    private Boolean guidePartageCommande;
    private Boolean guidePartagePerformance;
    private Boolean procedureResiliation;
    private Boolean modificationPuissance;
    private Boolean consommationsNullesIdentifiees;
    private Boolean estimationsRecensees;
    private Boolean batteriesCondensateursInstallees;
    private Boolean cadastreEnergetiqueRealise;
    private Boolean indexTransmis;
    private Boolean plateformeDigitale;
    private String autresActivites;
    private String contraintes;
    private String recommandations;

    // Section 4
    private String illustrationsName;
    private String autresDocumentsName;

    public ReportResponseDTO(Report report) {
        this.id                             = report.getId();
        this.reportStatus                   = report.getReportStatus().name();
        this.createdByUserId                = report.getCreatedByUserId();
        this.createdAt                      = report.getCreatedAt();
        this.updatedAt                      = report.getUpdatedAt();
        this.reportDate                     = report.getReportDate();
        this.nomGestionnaire                = report.getNomGestionnaire();
        this.serviceAppartenance            = report.getServiceAppartenance();
        this.nombreBatiments                = report.getNombreBatiments();
        this.numeroPoliceSenelec            = report.getNumeroPoliceSenelec();
        this.campagnesCommunication         = report.getCampagnesCommunication();
        this.guidePartageCommande           = report.getGuidePartageCommande();
        this.guidePartagePerformance        = report.getGuidePartagePerformance();
        this.procedureResiliation           = report.getProcedureResiliation();
        this.modificationPuissance          = report.getModificationPuissance();
        this.consommationsNullesIdentifiees = report.getConsommationsNullesIdentifiees();
        this.estimationsRecensees           = report.getEstimationsRecensees();
        this.batteriesCondensateursInstallees = report.getBatteriesCondensateursInstallees();
        this.cadastreEnergetiqueRealise     = report.getCadastreEnergetiqueRealise();
        this.indexTransmis                  = report.getIndexTransmis();
        this.plateformeDigitale             = report.getPlateformeDigitale();
        this.autresActivites                = report.getAutresActivites();
        this.contraintes                    = report.getContraintes();
        this.recommandations                = report.getRecommandations();
        this.illustrationsName              = report.getIllustrationsName();
        this.autresDocumentsName            = report.getAutresDocumentsName();
    }

    public Long getId() { 
        return id; 
    }
    public String getReportStatus() { 
        return reportStatus; 
    }
    public String getCreatedByUserId() { 
        return createdByUserId; 
    }
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
    public LocalDateTime getReportDate() { 
        return reportDate; 
    }
    public String getNomGestionnaire() { 
        return nomGestionnaire; 
    }
    public String getServiceAppartenance() { 
        return serviceAppartenance; 
    }
    public Integer getNombreBatiments() { 
        return nombreBatiments; 
    }
    public String getNumeroPoliceSenelec() { 
        return numeroPoliceSenelec; 
    }
    public String getCampagnesCommunication() { 
        return campagnesCommunication; 
    }
    public Boolean getGuidePartageCommande() { 
        return guidePartageCommande; 
    }
    public Boolean getGuidePartagePerformance() { 
        return guidePartagePerformance; 
    }
    public Boolean getProcedureResiliation() { 
        return procedureResiliation; 
    }
    public Boolean getModificationPuissance() { 
        return modificationPuissance; 
    }
    public Boolean getConsommationsNullesIdentifiees() { 
        return consommationsNullesIdentifiees; 
    }
    public Boolean getEstimationsRecensees() { 
        return estimationsRecensees; 
    }
    public Boolean getBatteriesCondensateursInstallees() { 
        return batteriesCondensateursInstallees; 
    }
    public Boolean getCadastreEnergetiqueRealise() { 
        return cadastreEnergetiqueRealise; 
    }
    public Boolean getIndexTransmis() { 
        return indexTransmis; 
    }
    public Boolean getPlateformeDigitale() { 
        return plateformeDigitale; 
    }
    public String getAutresActivites() { 
        return autresActivites; 
    }
    public String getContraintes() { 
        return contraintes; 
    }
    public String getRecommandations() { 
        return recommandations; 
    }
    public String getIllustrationsName() { 
        return illustrationsName; 
    }
    public String getAutresDocumentsName() { 
        return autresDocumentsName; 
    }
}