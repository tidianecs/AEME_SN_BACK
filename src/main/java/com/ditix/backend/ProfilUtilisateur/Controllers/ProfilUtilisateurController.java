package com.ditix.backend.ProfilUtilisateur.Controllers;

import com.ditix.backend.ProfilUtilisateur.DTO.ProfilUtilisateurDTO;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.AutorisationMetierService;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurLectureService;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v2")
public class ProfilUtilisateurController {

    private final ProfilUtilisateurCourantService profilUtilisateurCourantService;
    private final AutorisationMetierService autorisationMetierService;
    private final ProfilUtilisateurLectureService profilUtilisateurLectureService;
    private final StructureRepository structureRepository;

    public ProfilUtilisateurController(
            ProfilUtilisateurCourantService profilUtilisateurCourantService,
            AutorisationMetierService autorisationMetierService,
            ProfilUtilisateurLectureService profilUtilisateurLectureService,
            StructureRepository structureRepository) {
        this.profilUtilisateurCourantService = profilUtilisateurCourantService;
        this.autorisationMetierService = autorisationMetierService;
        this.profilUtilisateurLectureService = profilUtilisateurLectureService;
        this.structureRepository = structureRepository;
    }

    private Pageable createPageable(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paramètres de pagination invalides");
        }
        return PageRequest.of(page, size);
    }

    @GetMapping("/me")
    public ProfilUtilisateurDTO getMe(JwtAuthenticationToken authentication) {
        ProfilUtilisateur profil = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        return new ProfilUtilisateurDTO(profil);
    }

    @PatchMapping("/me")
    public ProfilUtilisateurDTO updateMe(
            JwtAuthenticationToken authentication,
            @RequestBody com.ditix.backend.ProfilUtilisateur.DTO.ModifierMonProfilRequest request) {
        return profilUtilisateurCourantService.modifierProfilCourant(authentication, request);
    }

    @GetMapping("/admin/utilisateurs")
    public Page<ProfilUtilisateurDTO> listAdminUsers(
            JwtAuthenticationToken authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ProfilUtilisateur profil = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        if (!autorisationMetierService.estAdmin(profil)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        }

        return profilUtilisateurLectureService.listerTous(createPageable(page, size));
    }

    @GetMapping("/admin/utilisateurs/{id}")
    public ProfilUtilisateurDTO getAdminUser(
            @PathVariable Long id,
            JwtAuthenticationToken authentication) {

        ProfilUtilisateur profil = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        if (!autorisationMetierService.estAdmin(profil)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        }

        return profilUtilisateurLectureService.obtenirParId(id);
    }

    @GetMapping("/dage/gestionnaires")
    public Page<ProfilUtilisateurDTO> listDageGestionnaires(
            JwtAuthenticationToken authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ProfilUtilisateur profil = profilUtilisateurCourantService.obtenirProfilCourant(authentication);
        if (!autorisationMetierService.estDage(profil)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        }

        if (profil.getMinistere() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Aucun ministère associé à ce DAGE");
        }

        return profilUtilisateurLectureService.listerGestionnairesParMinistere(profil.getMinistere().getId(), createPageable(page, size));
    }

    @GetMapping("/structures/{structureId}/gestionnaires")
    public Page<ProfilUtilisateurDTO> listStructureGestionnaires(
            @PathVariable Long structureId,
            JwtAuthenticationToken authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ProfilUtilisateur profil = profilUtilisateurCourantService.obtenirProfilCourant(authentication);

        Structure structure = structureRepository.findById(structureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Structure introuvable"));

        if (!autorisationMetierService.aAccesStructure(profil, structure)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à cette structure");
        }

        return profilUtilisateurLectureService.listerGestionnairesParStructure(structureId, createPageable(page, size));
    }
}
