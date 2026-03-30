package com.ditix.backend.Auth.Controllers;

import com.ditix.backend.Auth.Services.AuthService;
import com.ditix.backend.Report.DTO.ReportResponseDTO;
import com.ditix.backend.Report.Services.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AuthService authService;
    private final ReportService reportService;

    public AdminController(AuthService authService, ReportService reportService) {
        this.authService = authService;
        this.reportService = reportService;
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, String>> inviteUser(
            @RequestBody Map<String, String> body
    ) {
        String email             = body.get("email");
        String firstName         = body.get("firstName");
        String lastName          = body.get("lastName");
        String role              = body.getOrDefault("role", "user");
        String membershipService = body.getOrDefault("membershipService", "");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Email requis"));
        }
        if (!role.equals("user") && !role.equals("admin")) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Rôle invalide — utilise 'user' ou 'admin'"));
        }

        authService.inviteUser(email, firstName, lastName, role, membershipService);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Invitation envoyée à " + email));
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, String>>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PatchMapping("/users/{userId}/membership")
    public ResponseEntity<Map<String, String>> updateMembership(
            @PathVariable String userId,
            @RequestBody Map<String, String> body
    ) {
        String membershipService = body.get("membershipService");
        if (membershipService == null || membershipService.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "membershipService requis"));
        }
        authService.updateMembershipService(userId, membershipService);
        return ResponseEntity.ok(Map.of("message", "Membership mis à jour"));
    }

    @GetMapping("/users/{userId}/reports")
    public ResponseEntity<List<ReportResponseDTO>> getUserReports(@PathVariable String userId) {
        return ResponseEntity.ok(reportService.getReportsByUserId(userId));
    }

    @PatchMapping("/reports/{id}/status")
    public ResponseEntity<ReportResponseDTO> updateReportStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        if ("APPROVED".equals(status)) {
            return ResponseEntity.ok(reportService.approveReport(id));
        } else if ("REJECTED".equals(status)) {
            return ResponseEntity.ok(reportService.rejectReport(id));
        } else {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Status invalide — utilise 'APPROVED' ou 'REJECTED'");
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé"));
    }

    // @DeleteMapping("/reports/{id}")
    // public ResponseEntity<Map<String, String>> deleteReportAdmin(@PathVariable Long id) {
    //     reportService.deleteReportAdmin(id);
    //     return ResponseEntity.ok(Map.of("message", "Rapport supprimé"));
    // }
}