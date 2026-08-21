package com.ditix.backend.ProfilUtilisateur.Services;
import com.ditix.backend.ProfilUtilisateur.DTO.ProfilUtilisateurDTO;
import com.ditix.backend.ProfilUtilisateur.DTO.AdminUtilisateurDetailDTO;
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
    private final GestionCompteKeycloakService gestionCompteKeycloakService;
    public ProfilUtilisateurLectureService(
            ProfilUtilisateurRepository profilUtilisateurRepository,
            GestionCompteKeycloakService gestionCompteKeycloakService) {
        this.profilUtilisateurRepository = profilUtilisateurRepository;
        this.gestionCompteKeycloakService = gestionCompteKeycloakService;
    }
    public Page<ProfilUtilisateurDTO> listerTous(Pageable pageable) {
        return profilUtilisateurRepository.findAll(pageable).map(ProfilUtilisateurDTO::new);
    }
    public ProfilUtilisateurDTO obtenirParId(Long id) {
        return profilUtilisateurRepository.findById(id)
                .map(ProfilUtilisateurDTO::new)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }
    public AdminUtilisateurDetailDTO obtenirDetailAdminParId(Long id) {
        ProfilUtilisateur profil = profilUtilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        Boolean invitationPending = false;
        if (profil.getKeycloakId() != null) {
            invitationPending = gestionCompteKeycloakService.isInvitationPending(profil.getKeycloakId());
        }
        return new AdminUtilisateurDetailDTO(profil, invitationPending);
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
