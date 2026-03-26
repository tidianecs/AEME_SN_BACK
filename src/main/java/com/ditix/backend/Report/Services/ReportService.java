package com.ditix.backend.Report.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.ditix.backend.Report.DTO.ReportResponseDTO;
import com.ditix.backend.Report.Model.Report;
import com.ditix.backend.Report.Repository.ReportRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public ReportResponseDTO createReport(
            String reportType,
            LocalDateTime reportDate,
            String reportLocation,
            String reportDesc,
            MultipartFile file,
            String userId
    ) throws IOException {

        // Crée le dossier uploads s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Sauvegarde le fichier avec un nom unique
        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath);

        // Crée et sauvegarde le report
        Report report = new Report();
        report.setReportType(reportType);
        report.setReportDate(reportDate);
        report.setReportLocation(reportLocation);
        report.setReportDesc(reportDesc);
        report.setFileName(file.getOriginalFilename());
        report.setFilePath(filePath.toString());
        report.setContentType(file.getContentType());
        report.setFileSize(file.getSize());
        report.setCreatedByUserId(userId);

        return new ReportResponseDTO(reportRepository.save(report));
    }

    public List<ReportResponseDTO> getMyReports(String userId, String reportType) {
        List<Report> reports;
        if (reportType != null && !reportType.isEmpty()) {
            reports = reportRepository.findByCreatedByUserIdAndReportType(userId, reportType);
        } else {
            reports = reportRepository.findByCreatedByUserId(userId);
        }
        return reports.stream()
                .map(ReportResponseDTO::new)
                .collect(Collectors.toList());
    }

    public ReportResponseDTO getReportById(Long id, String userId) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rapport introuvable"));

        if (!report.getCreatedByUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        return new ReportResponseDTO(report);
    }

    public void deleteReport(Long id, String userId) throws IOException {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rapport introuvable"));

        if (!report.getCreatedByUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        // Supprime le fichier du disque
        if (report.getFilePath() != null) {
            Files.deleteIfExists(Paths.get(report.getFilePath()));
        }

        reportRepository.delete(report);
    }

    public Path getFilePath(Long id, String userId) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rapport introuvable"));

        if (!report.getCreatedByUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        return Paths.get(report.getFilePath());
    }

    public Report getRawReport(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rapport introuvable"));
    }
}
