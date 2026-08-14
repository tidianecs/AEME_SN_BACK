package com.ditix.backend.Notification.Controllers;

import com.ditix.backend.Notification.DTO.NotificationDTO;
import com.ditix.backend.Notification.Services.NotificationService;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Services.ProfilUtilisateurCourantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final ProfilUtilisateurCourantService profilService;

    public NotificationController(NotificationService notificationService, ProfilUtilisateurCourantService profilService) {
        this.notificationService = notificationService;
        this.profilService = profilService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(JwtAuthenticationToken auth) {
        ProfilUtilisateur profil = profilService.obtenirProfilCourant(auth);
        return ResponseEntity.ok(notificationService.getMyNotifications(profil));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(JwtAuthenticationToken auth) {
        ProfilUtilisateur profil = profilService.obtenirProfilCourant(auth);
        long count = notificationService.getUnreadCount(profil);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id, JwtAuthenticationToken auth) {
        ProfilUtilisateur profil = profilService.obtenirProfilCourant(auth);
        notificationService.markAsRead(id, profil);
        return ResponseEntity.ok(Map.of("message", "Notification marquée comme lue"));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(JwtAuthenticationToken auth) {
        ProfilUtilisateur profil = profilService.obtenirProfilCourant(auth);
        notificationService.markAllAsRead(profil);
        return ResponseEntity.ok(Map.of("message", "Toutes les notifications ont été marquées comme lues"));
    }
}
