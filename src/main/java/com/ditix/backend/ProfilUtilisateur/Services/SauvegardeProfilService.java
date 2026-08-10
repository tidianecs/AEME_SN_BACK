package com.ditix.backend.ProfilUtilisateur.Services;

import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.ProfilUtilisateur.DTO.CreerUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import com.ditix.backend.Structure.Model.Structure;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SauvegardeProfilService {

    private final ProfilUtilisateurRepository profilUtilisateurRepository;

    public SauvegardeProfilService(ProfilUtilisateurRepository profilUtilisateurRepository) {
        this.profilUtilisateurRepository = profilUtilisateurRepository;
    }

    @Transactional
    public ProfilUtilisateur sauvegarder(CreerUtilisateurRequest request, UUID keycloakId, Ministere ministere, Structure structure, Cohorte cohorte) {
        ProfilUtilisateur profil = new ProfilUtilisateur();
        profil.setKeycloakId(keycloakId);
        profil.setRole(request.getRole());
        profil.setActif(true);

        profil.setPrenom(request.getPrenom());
        profil.setNom(request.getNom());
        profil.setEmail(request.getEmail());
        profil.setTelephonePrincipal(request.getTelephonePrincipal());
        profil.setTelephoneSecondaire(request.getTelephoneSecondaire());
        profil.setEmailSecondaire(request.getEmailSecondaire());
        profil.setGenre(request.getGenre());
        profil.setDateNaissance(request.getDateNaissance());
        profil.setDepartementAdministratif(request.getDepartementAdministratif());
        profil.setPosteOccupe(request.getPosteOccupe());
        profil.setDateNomination(request.getDateNomination());
        profil.setDateInstallation(request.getDateInstallation());
        profil.setDateFormation(request.getDateFormation());
        profil.setDerniereMiseANiveau(request.getDerniereMiseANiveau());

        profil.setMinistere(ministere);
        profil.setStructure(structure);
        profil.setCohorte(cohorte);

        return profilUtilisateurRepository.saveAndFlush(profil);
    }
}
