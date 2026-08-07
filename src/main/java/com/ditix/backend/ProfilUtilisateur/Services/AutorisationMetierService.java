package com.ditix.backend.ProfilUtilisateur.Services;

import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.Structure.Model.Structure;
import org.springframework.stereotype.Service;

@Service
public class AutorisationMetierService {

    public boolean estAdmin(ProfilUtilisateur profil) {
        if (profil == null) return false;
        return RoleUtilisateur.ADMIN.equals(profil.getRole());
    }

    public boolean estDage(ProfilUtilisateur profil) {
        if (profil == null) return false;
        return RoleUtilisateur.DAGE.equals(profil.getRole());
    }

    public boolean estGestionnaire(ProfilUtilisateur profil) {
        if (profil == null) return false;
        return RoleUtilisateur.GESTIONNAIRE.equals(profil.getRole());
    }

    public boolean aAccesMinistere(ProfilUtilisateur profil, Long ministereId) {
        if (profil == null || ministereId == null) {
            return false;
        }

        if (estAdmin(profil)) {
            return true;
        }

        if (estDage(profil) && profil.getMinistere() != null) {
            return ministereId.equals(profil.getMinistere().getId());
        }

        if (estGestionnaire(profil) && profil.getStructure() != null && profil.getStructure().getMinistereV2() != null) {
            return ministereId.equals(profil.getStructure().getMinistereV2().getId());
        }

        return false;
    }

    public boolean aAccesStructure(ProfilUtilisateur profil, Structure structure) {
        if (profil == null || structure == null) {
            return false;
        }

        if (estAdmin(profil)) {
            return true;
        }

        if (estDage(profil) && profil.getMinistere() != null && structure.getMinistereV2() != null) {
            return profil.getMinistere().getId().equals(structure.getMinistereV2().getId());
        }

        if (estGestionnaire(profil) && profil.getStructure() != null) {
            return profil.getStructure().getId().equals(structure.getId());
        }

        return false;
    }
}
