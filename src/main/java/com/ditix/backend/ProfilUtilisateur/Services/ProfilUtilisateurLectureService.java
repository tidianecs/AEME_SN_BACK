package com.ditix.backend.ProfilUtilisateur.Services;

import com.ditix.backend.ProfilUtilisateur.DTO.ProfilUtilisateurDTO;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfilUtilisateurLectureService {

    private final ProfilUtilisateurRepository profilUtilisateurRepository;

    public ProfilUtilisateurLectureService(ProfilUtilisateurRepository profilUtilisateurRepository) {
        this.profilUtilisateurRepository = profilUtilisateurRepository;
    }

    public Page<ProfilUtilisateurDTO> listerTous(Pageable pageable) {
        return profilUtilisateurRepository.findAll(pageable).map(ProfilUtilisateurDTO::new);
    }

    public ProfilUtilisateurDTO obtenirParId(Long id) {
        return profilUtilisateurRepository.findById(id)
                .map(ProfilUtilisateurDTO::new)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    public Page<ProfilUtilisateurDTO> listerGestionnairesParMinistere(Long ministereId, Pageable pageable) {
        return profilUtilisateurRepository.findGestionnairesByMinistereId(ministereId, pageable)
                .map(ProfilUtilisateurDTO::new);
    }

    public Page<ProfilUtilisateurDTO> listerGestionnairesParStructure(Long structureId, Pageable pageable) {
        return profilUtilisateurRepository.findGestionnairesByStructureId(structureId, pageable)
                .map(ProfilUtilisateurDTO::new);
    }
}
