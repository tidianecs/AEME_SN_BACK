package com.ditix.backend.Meeting.Controllers;

import com.ditix.backend.Meeting.DTO.CreateManagedMeetingRequest;
import com.ditix.backend.Meeting.DTO.MeetingResponseDTO;
import com.ditix.backend.Meeting.Services.MeetingAdminService;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/admin/meetings")
public class MeetingAdminController {

    private final MeetingAdminService meetingAdminService;
    private final ProfilUtilisateurCourantService profilService;

    public MeetingAdminController(MeetingAdminService meetingAdminService, ProfilUtilisateurCourantService profilService) {
        this.meetingAdminService = meetingAdminService;
        this.profilService = profilService;
    }

    private ProfilUtilisateur requireAdmin(JwtAuthenticationToken auth) {
        ProfilUtilisateur profil = profilService.obtenirProfilCourant(auth);
        if (profil.getRole() != RoleUtilisateur.ADMIN || !Boolean.TRUE.equals(profil.getActif())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès admin actif requis");
        }
        return profil;
    }

    @PostMapping("/global")
    public ResponseEntity<MeetingResponseDTO> createGlobal(
            @RequestBody CreateManagedMeetingRequest request,
            JwtAuthenticationToken auth
    ) {
        ProfilUtilisateur admin = requireAdmin(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meetingAdminService.createGlobalMeeting(request, admin));
    }

    @PostMapping("/cohort")
    public ResponseEntity<MeetingResponseDTO> createCohort(
            @RequestBody CreateManagedMeetingRequest request,
            JwtAuthenticationToken auth
    ) {
        ProfilUtilisateur admin = requireAdmin(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meetingAdminService.createCohortMeeting(request, admin));
    }

    @PostMapping("/structure")
    public ResponseEntity<MeetingResponseDTO> createStructure(
            @RequestBody CreateManagedMeetingRequest request,
            JwtAuthenticationToken auth
    ) {
        ProfilUtilisateur admin = requireAdmin(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meetingAdminService.createStructureMeeting(request, admin));
    }

    @PostMapping("/ministere")
    public ResponseEntity<MeetingResponseDTO> createMinistere(
            @RequestBody CreateManagedMeetingRequest request,
            JwtAuthenticationToken auth
    ) {
        ProfilUtilisateur admin = requireAdmin(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meetingAdminService.createMinistereMeeting(request, admin));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MeetingResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            JwtAuthenticationToken auth
    ) {
        ProfilUtilisateur admin = requireAdmin(auth);
        return ResponseEntity.ok(meetingAdminService.updateStatus(id, body.get("status"), admin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMeeting(
            @PathVariable Long id,
            JwtAuthenticationToken auth
    ) {
        ProfilUtilisateur admin = requireAdmin(auth);
        meetingAdminService.deleteMeeting(id, admin);
        return ResponseEntity.ok(Map.of("message", "Meeting supprimé"));
    }
}
