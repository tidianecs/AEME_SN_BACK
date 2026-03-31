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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponseDTO> createReport(
            @RequestParam("reportDate")                     String reportDate,
            @RequestParam("nomGestionnaire")                String nomGestionnaire,
            @RequestParam("serviceAppartenance")            String serviceAppartenance,
            @RequestParam("nombreBatiments")                Integer nombreBatiments,
            @RequestParam("numeroPoliceSenelec")            String numeroPoliceSenelec,
            @RequestParam(value = "campagnesCommunication", required = false) String campagnesCommunication,
            @RequestParam(value = "guidePartageCommande",   required = false) Boolean guidePartageCommande,
            @RequestParam(value = "guidePartagePerformance",required = false) Boolean guidePartagePerformance,
            @RequestParam(value = "procedureResiliation",   required = false) Boolean procedureResiliation,
            @RequestParam(value = "modificationPuissance",  required = false) Boolean modificationPuissance,
            @RequestParam(value = "consommationsNullesIdentifiees", required = false) Boolean consommationsNullesIdentifiees,
            @RequestParam(value = "estimationsRecensees",   required = false) Boolean estimationsRecensees,
            @RequestParam(value = "batteriesCondensateursInstallees", required = false) Boolean batteriesCondensateursInstallees,
            @RequestParam(value = "cadastreEnergetiqueRealise", required = false) Boolean cadastreEnergetiqueRealise,
            @RequestParam(value = "indexTransmis",          required = false) Boolean indexTransmis,
            @RequestParam(value = "plateformeDigitale",     required = false) Boolean plateformeDigitale,
            @RequestParam(value = "autresActivites",        required = false) String autresActivites,
            @RequestParam(value = "contraintes",            required = false) String contraintes,
            @RequestParam(value = "recommandations",        required = false) String recommandations,
            @RequestParam(value = "illustrations",          required = false) MultipartFile illustrations,
            @RequestParam(value = "autresDocuments",        required = false) MultipartFile autresDocuments,
            JwtAuthenticationToken authentication
    ) throws IOException {
        String userId = authentication.getToken().getSubject();
        ReportResponseDTO response = reportService.createReport(
                LocalDateTime.parse(reportDate),
                nomGestionnaire,
                serviceAppartenance,
                nombreBatiments,
                numeroPoliceSenelec,
                campagnesCommunication,
                guidePartageCommande,
                guidePartagePerformance,
                procedureResiliation,
                modificationPuissance,
                consommationsNullesIdentifiees,
                estimationsRecensees,
                batteriesCondensateursInstallees,
                cadastreEnergetiqueRealise,
                indexTransmis,
                plateformeDigitale,
                autresActivites,
                contraintes,
                recommandations,
                illustrations,
                autresDocuments,
                userId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> getMyReports(
            JwtAuthenticationToken authentication
    ) {
        String userId = authentication.getToken().getSubject();
        return ResponseEntity.ok(reportService.getMyReports(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDTO> getReportById(
            @PathVariable Long id,
            JwtAuthenticationToken authentication
    ) {
        String userId = authentication.getToken().getSubject();
        return ResponseEntity.ok(reportService.getReportById(id, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteReport(
            @PathVariable Long id,
            JwtAuthenticationToken authentication
    ) throws IOException {
        String userId = authentication.getToken().getSubject();
        reportService.deleteReport(id, userId);
        return ResponseEntity.ok(Map.of("message", "Rapport supprimé"));
    }

    @GetMapping("/{id}/download/{fileType}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            @PathVariable String fileType,
            JwtAuthenticationToken authentication
    ) throws IOException {
        String userId = authentication.getToken().getSubject();
        Report report = reportService.getRawReport(id);

        if (!report.getCreatedByUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String filePath = fileType.equals("illustrations")
                ? report.getIllustrationsPath()
                : report.getAutresDocumentsPath();

        String fileName = fileType.equals("illustrations")
                ? report.getIllustrationsName()
                : report.getAutresDocumentsName();

        if (filePath == null) return ResponseEntity.notFound().build();

        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}