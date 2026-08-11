package com.ditix.backend.Report.Controllers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ditix.backend.Report.DTO.ReportResponseDTO;
import com.ditix.backend.Report.Model.Report;
import com.ditix.backend.Report.Services.ReportService;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final ProfilUtilisateurCourantService profilUtilisateurCourantService;

    public ReportController(ReportService reportService, ProfilUtilisateurCourantService profilUtilisateurCourantService) {
        this.reportService = reportService;
        this.profilUtilisateurCourantService = profilUtilisateurCourantService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponseDTO> createReport(
            @RequestParam("reportDate")                                              String reportDate,
            @RequestParam("nomGestionnaire")                                         String nomGestionnaire,
            @RequestParam("serviceAppartenance")                                     String serviceAppartenance,
            @RequestParam("nombreBatiments")                                         Integer nombreBatiments,
            @RequestParam("numeroPoliceSenelec")                                     String numeroPoliceSenelec,
            @RequestParam(value = "campagnesCommunication",        required = false) String campagnesCommunication,
            @RequestParam(value = "autreCampagnePrecision",        required = false) String autreCampagnePrecision,
            @RequestParam(value = "guidePartageCommande",          required = false) Boolean guidePartageCommande,
            @RequestParam(value = "guidePartagePerformance",       required = false) Boolean guidePartagePerformance,
            @RequestParam(value = "procedureResiliation",          required = false) Boolean procedureResiliation,
            @RequestParam(value = "modificationPuissance",         required = false) Boolean modificationPuissance,
            @RequestParam(value = "pieceJustificativeModification",required = false) MultipartFile pieceJustificativeModification,
            @RequestParam(value = "consommationsNullesIdentifiees",required = false) Boolean consommationsNullesIdentifiees,
            @RequestParam(value = "actionConsommationsNulles",     required = false) String actionConsommationsNulles,
            @RequestParam(value = "estimationsRecensees",          required = false) Boolean estimationsRecensees,
            @RequestParam(value = "actionEstimations",             required = false) String actionEstimations,
            @RequestParam(value = "batteriesCondensateursInstallees", required = false) Boolean batteriesCondensateursInstallees,
            @RequestParam(value = "nombreBatteriesCondensateurs",  required = false) Integer nombreBatteriesCondensateurs,
            @RequestParam(value = "cadastreEnergetiqueRealise",    required = false) Boolean cadastreEnergetiqueRealise,
            @RequestParam(value = "indexTransmis",                 required = false) Boolean indexTransmis,
            @RequestParam(value = "dateIndexTransmis",             required = false) String dateIndexTransmis,
            @RequestParam(value = "indexConsommation",             required = false) String indexConsommation,
            @RequestParam(value = "plateformeDigitale",            required = false) Boolean plateformeDigitale,
            @RequestParam(value = "suiviPlateformeDigitale",       required = false) Boolean suiviPlateformeDigitale,
            @RequestParam(value = "autresActivites",               required = false) String autresActivites,
            @RequestParam(value = "autreActivitePrecision",        required = false) String autreActivitePrecision,
            @RequestParam(value = "contraintes",                   required = false) String contraintes,
            @RequestParam(value = "recommandations",               required = false) String recommandations,
            @RequestParam(value = "illustrations",                 required = false) MultipartFile illustrations,
            @RequestParam(value = "autresDocuments",               required = false) MultipartFile autresDocuments,
            JwtAuthenticationToken authentication
    ) throws IOException {
        ProfilUtilisateur profil = profilUtilisateurCourantService.obtenirProfilCourant(authentication);

        ReportResponseDTO response = reportService.createReport(
                LocalDateTime.parse(reportDate),
                nomGestionnaire, serviceAppartenance, nombreBatiments, numeroPoliceSenelec,
                campagnesCommunication, autreCampagnePrecision,
                guidePartageCommande, guidePartagePerformance, procedureResiliation,
                modificationPuissance, pieceJustificativeModification,
                consommationsNullesIdentifiees, actionConsommationsNulles,
                estimationsRecensees, actionEstimations,
                batteriesCondensateursInstallees, nombreBatteriesCondensateurs,
                cadastreEnergetiqueRealise,
                indexTransmis,
                dateIndexTransmis != null ? LocalDateTime.parse(dateIndexTransmis) : null,
                indexConsommation,
                plateformeDigitale, suiviPlateformeDigitale,
                autresActivites, autreActivitePrecision,
                contraintes, recommandations,
                illustrations, autresDocuments, profil
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> getMyReports(JwtAuthenticationToken authentication) {
        ProfilUtilisateur profil = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        String userId = profil.getKeycloakId().toString();
        return ResponseEntity.ok(reportService.getMyReports(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReportResponseDTO>> getAllReports(JwtAuthenticationToken authentication) {
        ProfilUtilisateur profil = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        return ResponseEntity.ok(reportService.getAllReports(profil));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDTO> getReportById(
            @PathVariable Long id,
            JwtAuthenticationToken authentication
    ) {
        ProfilUtilisateur profil = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        return ResponseEntity.ok(reportService.getReportById(id, profil));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteReport(
            @PathVariable Long id,
            JwtAuthenticationToken authentication
    ) throws IOException {
        ProfilUtilisateur profil = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        String userId = profil.getKeycloakId().toString();
        reportService.deleteReport(id, userId);
        return ResponseEntity.ok(Map.of("message", "Rapport supprimé"));
    }

    @GetMapping("/{id}/download/{fileType}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            @PathVariable String fileType,
            JwtAuthenticationToken authentication
    ) throws IOException {
        ProfilUtilisateur profil = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        Report report = reportService.getRawReport(id, profil);

        String filePath = switch (fileType) {
            case "illustrations"              -> report.getIllustrationsPath();
            case "autresDocuments"            -> report.getAutresDocumentsPath();
            case "pieceJustificativeModification" -> report.getPieceJustificativeModificationPath();
            default                           -> null;
        };

        String fileName = switch (fileType) {
            case "illustrations"              -> report.getIllustrationsName();
            case "autresDocuments"            -> report.getAutresDocumentsName();
            case "pieceJustificativeModification" -> report.getPieceJustificativeModificationName();
            default                           -> null;
        };

        if (filePath == null) return ResponseEntity.notFound().build();

        java.nio.file.Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}