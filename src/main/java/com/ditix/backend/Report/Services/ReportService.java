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

    private String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath);
        return filePath.toString();
    }

    public ReportResponseDTO createReport(
            LocalDateTime reportDate,
            String nomGestionnaire,
            String serviceAppartenance,
            Integer nombreBatiments,
            String numeroPoliceSenelec,
            String campagnesCommunication,
            Boolean guidePartageCommande,
            Boolean guidePartagePerformance,
            Boolean procedureResiliation,
            Boolean modificationPuissance,
            Boolean consommationsNullesIdentifiees,
            Boolean estimationsRecensees,
            Boolean batteriesCondensateursInstallees,
            Boolean cadastreEnergetiqueRealise,
            Boolean indexTransmis,
            Boolean plateformeDigitale,
            String autresActivites,
            String contraintes,
            String recommandations,
            MultipartFile illustrations,
            MultipartFile autresDocuments,
            String userId
    ) throws IOException {

        String illustrationsPath  = saveFile(illustrations);
        String autresDocumentsPath = saveFile(autresDocuments);

        Report report = Report.builder()
                .createdByUserId(userId)
                .reportDate(reportDate)
                .nomGestionnaire(nomGestionnaire)
                .serviceAppartenance(serviceAppartenance)
                .nombreBatiments(nombreBatiments)
                .numeroPoliceSenelec(numeroPoliceSenelec)
                .campagnesCommunication(campagnesCommunication)
                .guidePartageCommande(guidePartageCommande)
                .guidePartagePerformance(guidePartagePerformance)
                .procedureResiliation(procedureResiliation)
                .modificationPuissance(modificationPuissance)
                .consommationsNullesIdentifiees(consommationsNullesIdentifiees)
                .estimationsRecensees(estimationsRecensees)
                .batteriesCondensateursInstallees(batteriesCondensateursInstallees)
                .cadastreEnergetiqueRealise(cadastreEnergetiqueRealise)
                .indexTransmis(indexTransmis)
                .plateformeDigitale(plateformeDigitale)
                .autresActivites(autresActivites)
                .contraintes(contraintes)
                .recommandations(recommandations)
                .illustrationsPath(illustrationsPath)
                .illustrationsName(illustrations != null && !illustrations.isEmpty()
                        ? illustrations.getOriginalFilename() : null)
                .autresDocumentsPath(autresDocumentsPath)
                .autresDocumentsName(autresDocuments != null && !autresDocuments.isEmpty()
                        ? autresDocuments.getOriginalFilename() : null)
                .build();

        return new ReportResponseDTO(reportRepository.save(report));
    }

    public List<ReportResponseDTO> getMyReports(String userId) {
        return reportRepository.findByCreatedByUserId(userId).stream()
                .map(ReportResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<ReportResponseDTO> getAllReports() {
        return reportRepository.findAll().stream()
                .map(ReportResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Accessible à tous — plus de vérification ownership
    public ReportResponseDTO getReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Rapport introuvable"));
        return new ReportResponseDTO(report);
    }

    // Suppression — garde la vérification ownership
    public void deleteReport(Long id, String userId) throws IOException {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Rapport introuvable"));
        if (!report.getCreatedByUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }
        deleteFiles(report);
        reportRepository.delete(report);
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
        deleteFiles(report);
        reportRepository.delete(report);
    }

    private void deleteFiles(Report report) {
        try {
            if (report.getIllustrationsPath() != null)
                Files.deleteIfExists(Paths.get(report.getIllustrationsPath()));
            if (report.getAutresDocumentsPath() != null)
                Files.deleteIfExists(Paths.get(report.getAutresDocumentsPath()));
        } catch (IOException e) {
            System.out.println("Erreur suppression fichier : " + e.getMessage());
        }
    }
}