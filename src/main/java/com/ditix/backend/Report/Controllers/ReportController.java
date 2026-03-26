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

    // Créer un rapport
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponseDTO> createReport(
            @RequestParam("reportType") String reportType,
            @RequestParam("reportDate") String reportDate,
            @RequestParam("reportLocation") String reportLocation,
            @RequestParam("reportDesc") String reportDesc,
            @RequestParam("file") MultipartFile file,
            JwtAuthenticationToken authentication
    ) throws IOException {

        String userId = authentication.getToken().getSubject();
        ReportResponseDTO response = reportService.createReport(
                reportType,
                LocalDateTime.parse(reportDate),
                reportLocation,
                reportDesc,
                file,
                userId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Lister mes rapports
    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> getMyReports(
            @RequestParam(required = false) String reportType,
            JwtAuthenticationToken authentication
    ) {
        String userId = authentication.getToken().getSubject();
        return ResponseEntity.ok(reportService.getMyReports(userId, reportType));
    }

    // Détail d'un rapport
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDTO> getReportById(
            @PathVariable Long id,
            JwtAuthenticationToken authentication
    ) {
        String userId = authentication.getToken().getSubject();
        return ResponseEntity.ok(reportService.getReportById(id, userId));
    }

    // Supprimer un rapport
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteReport(
            @PathVariable Long id,
            JwtAuthenticationToken authentication
    ) throws IOException {
        String userId = authentication.getToken().getSubject();
        reportService.deleteReport(id, userId);
        return ResponseEntity.ok(Map.of("message", "Rapport supprimé"));
    }

    // Télécharger le fichier
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            JwtAuthenticationToken authentication
    ) throws IOException {
        String userId = authentication.getToken().getSubject();
        Path filePath = reportService.getFilePath(id, userId);
        Report report = reportService.getRawReport(id);

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(report.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + report.getFileName() + "\"")
                .body(resource);
    }
}
