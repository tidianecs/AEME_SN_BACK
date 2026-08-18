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
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Objects;
import java.util.stream.Collectors;
@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final ReportAutorisationService reportAutorisationService;

    public ReportService(ReportRepository reportRepository, ReportAutorisationService reportAutorisationService) {
        this.reportRepository = reportRepository;
        this.reportAutorisationService = reportAutorisationService;
    }

    private String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
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
            String autreCampagnePrecision,
            Boolean guidePartageCommande,
            Boolean guidePartagePerformance,
            Boolean procedureResiliation,
            Boolean modificationPuissance,
            MultipartFile pieceJustificativeModification,
            Boolean consommationsNullesIdentifiees,
            String actionConsommationsNulles,
            Boolean estimationsRecensees,
            String actionEstimations,
            Boolean batteriesCondensateursInstallees,
            Integer nombreBatteriesCondensateurs,
            Boolean cadastreEnergetiqueRealise,
            Boolean indexTransmis,
            LocalDateTime dateIndexTransmis,
            String indexConsommation,
            Boolean plateformeDigitale,
            Boolean suiviPlateformeDigitale,
            String autresActivites,
            String autreActivitePrecision,
            String contraintes,
            String recommandations,
            MultipartFile illustrations,
            MultipartFile autresDocuments,
            ProfilUtilisateur profilUtilisateur
    ) throws IOException {

        String illustrationsPath              = saveFile(illustrations);
        String autresDocumentsPath            = saveFile(autresDocuments);
        String pieceJustificativeModPath      = saveFile(pieceJustificativeModification);

        String userId = profilUtilisateur.getKeycloakId().toString();

        Report report = Report.builder()
                .createdByUserId(userId)
                .reportDate(reportDate)
                .nomGestionnaire(nomGestionnaire)
                .serviceAppartenance(serviceAppartenance)
                .nombreBatiments(nombreBatiments)
                .numeroPoliceSenelec(numeroPoliceSenelec)
                .campagnesCommunication(campagnesCommunication)
                .autreCampagnePrecision(autreCampagnePrecision)
                .guidePartageCommande(guidePartageCommande)
                .guidePartagePerformance(guidePartagePerformance)
                .procedureResiliation(procedureResiliation)
                .modificationPuissance(modificationPuissance)
                .pieceJustificativeModificationPath(pieceJustificativeModPath)
                .pieceJustificativeModificationName(
                    pieceJustificativeModification != null && !pieceJustificativeModification.isEmpty()
                        ? pieceJustificativeModification.getOriginalFilename() : null)
                .consommationsNullesIdentifiees(consommationsNullesIdentifiees)
                .actionConsommationsNulles(actionConsommationsNulles)
                .estimationsRecensees(estimationsRecensees)
                .actionEstimations(actionEstimations)
                .batteriesCondensateursInstallees(batteriesCondensateursInstallees)
                .nombreBatteriesCondensateurs(nombreBatteriesCondensateurs)
                .cadastreEnergetiqueRealise(cadastreEnergetiqueRealise)
                .indexTransmis(indexTransmis)
                .dateIndexTransmis(dateIndexTransmis)
                .indexConsommation(indexConsommation)
                .plateformeDigitale(plateformeDigitale)
                .suiviPlateformeDigitale(suiviPlateformeDigitale)
                .autresActivites(autresActivites)
                .autreActivitePrecision(autreActivitePrecision)
                .contraintes(contraintes)
                .recommandations(recommandations)
                .illustrationsPath(illustrationsPath)
                .illustrationsName(illustrations != null && !illustrations.isEmpty()
                        ? illustrations.getOriginalFilename() : null)
                .autresDocumentsPath(autresDocumentsPath)
                .autresDocumentsName(autresDocuments != null && !autresDocuments.isEmpty()
                        ? autresDocuments.getOriginalFilename() : null)
                .profilUtilisateur(profilUtilisateur)
                .build();

        report.setReportStatus(ReportStatus.SUBMITTED);

        return new ReportResponseDTO(reportRepository.save(report));
    }

    public List<ReportResponseDTO> getMyReports(String userId) {
        return reportRepository.findByCreatedByUserId(userId).stream()
                .map(ReportResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<ReportResponseDTO> getAllReports(ProfilUtilisateur profil) {
        List<Report> reports;
        if (profil != null && profil.getRole() == com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur.ADMIN) {
            reports = reportRepository.findAll();
        } else if (profil != null && profil.getRole() == com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur.DAGE) {
            if (profil.getMinistere() != null) {
                reports = reportRepository.findByMinistereIdAndRoleGestionnaire(profil.getMinistere().getId());
            } else {
                reports = List.of();
            }
        } else if (profil != null && profil.getRole() == com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur.GESTIONNAIRE) {
            reports = reportRepository.findByCreatedByUserId(profil.getKeycloakId().toString());
        } else {
            reports = List.of();
        }

        return reports.stream()
                .map(ReportResponseDTO::new)
                .collect(Collectors.toList());
    }

    private Report getAccessibleReport(Long reportId, ProfilUtilisateur profil) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rapport introuvable"));

        if (reportAutorisationService.peutLireRapport(report, profil)) {
            return report;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à ce rapport");
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ReportResponseDTO getReportById(Long id, ProfilUtilisateur profil) {
        Report report = getAccessibleReport(id, profil);
        return new ReportResponseDTO(report);
    }

    public void deleteReport(Long id, String userId) throws IOException {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rapport introuvable"));
        if (!report.getCreatedByUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }
        deleteFiles(report);
        reportRepository.delete(report);
    }

    public ReportResponseDTO approveReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rapport introuvable"));
        if (report.getReportStatus() != ReportStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seul un rapport SUBMITTED peut être approuvé");
        }
        report.setReportStatus(ReportStatus.APPROVED);
        return new ReportResponseDTO(reportRepository.save(report));
    }

    public ReportResponseDTO rejectReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rapport introuvable"));
        if (report.getReportStatus() != ReportStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seul un rapport SUBMITTED peut être rejeté");
        }
        report.setReportStatus(ReportStatus.REJECTED);
        return new ReportResponseDTO(reportRepository.save(report));
    }

    public int calculateScore(String userId) {
        long approved = reportRepository.countByCreatedByUserIdAndReportStatus(userId, ReportStatus.APPROVED);
        long rejected = reportRepository.countByCreatedByUserIdAndReportStatus(userId, ReportStatus.REJECTED);
        return (int) (approved * 4 - rejected * 5);
    }

    public Report getRawReport(Long id, ProfilUtilisateur profil) {
        return getAccessibleReport(id, profil);
    }

    public List<ReportResponseDTO> getReportsByUserId(String userId) {
        return reportRepository.findByCreatedByUserId(userId).stream()
                .map(ReportResponseDTO::new)
                .collect(Collectors.toList());
    }

    public void deleteReportAdmin(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rapport introuvable"));
        deleteFiles(report);
        reportRepository.delete(report);
    }

    private void deleteFiles(Report report) {
        try {
            if (report.getIllustrationsPath() != null)
                Files.deleteIfExists(Paths.get(report.getIllustrationsPath()));
            if (report.getAutresDocumentsPath() != null)
                Files.deleteIfExists(Paths.get(report.getAutresDocumentsPath()));
            if (report.getPieceJustificativeModificationPath() != null)
                Files.deleteIfExists(Paths.get(report.getPieceJustificativeModificationPath()));
        } catch (IOException e) {
            System.out.println("Erreur suppression fichier : " + e.getMessage());
        }
    }
}