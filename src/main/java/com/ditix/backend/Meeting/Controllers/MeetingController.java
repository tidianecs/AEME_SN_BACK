package com.ditix.backend.Meeting.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.ditix.backend.Meeting.Services.MeetingService;
import com.ditix.backend.Meeting.DTO.MeetingResponseDTO;
import com.ditix.backend.Meeting.DTO.CreateMeetingRequest;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;

@RestController
@RequestMapping("/api/v1/meetings")
public class MeetingController {

    private final MeetingService meetingService;
    private final ProfilUtilisateurCourantService profilService;

    public MeetingController(MeetingService meetingService, ProfilUtilisateurCourantService profilService) {
        this.meetingService = meetingService;
        this.profilService = profilService;
    }

    // Créer un meeting
    @PostMapping
    public ResponseEntity<MeetingResponseDTO> createMeeting(
            @RequestBody CreateMeetingRequest request,
            JwtAuthenticationToken authentication
    ) {
        String userId = authentication.getToken().getSubject();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(meetingService.createMeeting(request, userId));
    }

    // Lister mes meetings
    @GetMapping
    public ResponseEntity<List<MeetingResponseDTO>> getMyMeetings(
            JwtAuthenticationToken authentication
    ) {
        ProfilUtilisateur profil = profilService.obtenirProfilCourant(authentication);
        return ResponseEntity.ok(meetingService.getMyMeetings(profil));
    }

    // Détail d'un meeting
    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponseDTO> getMeetingById(
            @PathVariable Long id,
            JwtAuthenticationToken authentication
    ) {
        ProfilUtilisateur profil = profilService.obtenirProfilCourant(authentication);
        return ResponseEntity.ok(meetingService.getMeetingById(id, profil));
    }

    // Mettre à jour le statut
    @PatchMapping("/{id}/status")
    public ResponseEntity<MeetingResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            JwtAuthenticationToken authentication
    ) {
        String userId = authentication.getToken().getSubject();
        return ResponseEntity.ok(meetingService.updateStatus(id, body.get("status"), userId));
    }

    // Supprimer un meeting
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMeeting(
            @PathVariable Long id,
            JwtAuthenticationToken authentication
    ) {
        String userId = authentication.getToken().getSubject();
        meetingService.deleteMeeting(id, userId);
        return ResponseEntity.ok(Map.of("message", "Meeting supprimé"));
    }
}