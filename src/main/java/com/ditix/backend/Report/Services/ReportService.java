package com.ditix.backend.Report.Services;

import com.ditix.backend.Report.Model.ReportStatus;
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
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath);

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
        report.setReportStatus(ReportStatus.SUBMITTED);

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
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Rapport introuvable"));
        if (!report.getCreatedByUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }
        return new ReportResponseDTO(report);
    }

    public void deleteReport(Long id, String userId) throws IOException {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Rapport introuvable"));
        if (!report.getCreatedByUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }
        if (report.getFilePath() != null) {
            Files.deleteIfExists(Paths.get(report.getFilePath()));
        }
        reportRepository.delete(report);
    }

    public Path getFilePath(Long id, String userId) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Rapport introuvable"));
        if (!report.getCreatedByUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }
        return Paths.get(report.getFilePath());
    }

    public ReportResponseDTO approveReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Rapport introuvable"));
        if (report.getReportStatus() != ReportStatus.SUBMITTED) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Seul un rapport SUBMITTED peut être approuvé");
        }
        report.setReportStatus(ReportStatus.APPROVED);
        return new ReportResponseDTO(reportRepository.save(report));
    }

    public ReportResponseDTO rejectReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Rapport introuvable"));
        if (report.getReportStatus() != ReportStatus.SUBMITTED) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Seul un rapport SUBMITTED peut être rejeté");
        }
        report.setReportStatus(ReportStatus.REJECTED);
        return new ReportResponseDTO(reportRepository.save(report));
    }

    public int calculateScore(String userId) {
        long approved = reportRepository.countByCreatedByUserIdAndReportStatus(
            userId, ReportStatus.APPROVED);
        long rejected = reportRepository.countByCreatedByUserIdAndReportStatus(
            userId, ReportStatus.REJECTED);
        return (int) (approved * 4 - rejected * 5);
    }

    public Report getRawReport(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Rapport introuvable"));
    }

    public List<ReportResponseDTO> getReportsByUserId(String userId) {
        return reportRepository.findByCreatedByUserId(userId).stream()
            .map(ReportResponseDTO::new)
            .collect(Collectors.toList());
    }

    public void deleteReportAdmin(Long id) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Rapport introuvable"));
        try {
            if (report.getFilePath() != null) {
                Files.deleteIfExists(Paths.get(report.getFilePath()));
            }
        } catch (IOException e) {
            // log mais on continue
        }
        reportRepository.delete(report);
    }
}