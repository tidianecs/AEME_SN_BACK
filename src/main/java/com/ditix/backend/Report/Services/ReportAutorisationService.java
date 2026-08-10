package com.ditix.backend.Report.Services;

import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.AutorisationMetierService;
import com.ditix.backend.Report.Model.Report;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ReportAutorisationService {

    private final AutorisationMetierService autorisationMetierService;

    public ReportAutorisationService(AutorisationMetierService autorisationMetierService) {
        this.autorisationMetierService = autorisationMetierService;
    }

    public boolean peutLireRapport(Report report, ProfilUtilisateur profil) {
        if (report == null || profil == null) {
            return false;
        }

        if (autorisationMetierService.estAdmin(profil)) {
            return true;
        }

        if (autorisationMetierService.estGestionnaire(profil)) {
            return Objects.equals(report.getCreatedByUserId(), profil.getKeycloakId().toString());
        }

        if (autorisationMetierService.estDage(profil)) {
            if (report.getProfilUtilisateur() == null) {
                return false; // Legacy reports without profile are not accessible to DAGE
            }
            if (!autorisationMetierService.estGestionnaire(report.getProfilUtilisateur())) {
                return false;
            }
            if (profil.getMinistere() == null || report.getProfilUtilisateur().getStructure() == null || report.getProfilUtilisateur().getStructure().getMinistereV2() == null) {
                return false;
            }
            return profil.getMinistere().getId().equals(report.getProfilUtilisateur().getStructure().getMinistereV2().getId());
        }

        return false;
    }
}
