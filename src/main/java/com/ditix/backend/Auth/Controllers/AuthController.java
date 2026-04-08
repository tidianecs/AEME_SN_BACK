package com.ditix.backend.Auth.Controllers;

import com.ditix.backend.Auth.Services.AuthService;
import com.ditix.backend.Report.Services.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final ReportService reportService;

    public AuthController(AuthService authService, ReportService reportService) {
        this.authService = authService;
        this.reportService = reportService;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(JwtAuthenticationToken authentication) {
        Jwt jwt = authentication.getToken();
        return ResponseEntity.ok(Map.of(
                "id",        jwt.getSubject(),
                "email",     jwt.getClaimAsString("email"),
                "firstName", jwt.getClaimAsString("given_name"),
                "lastName",  jwt.getClaimAsString("family_name"),
                "fullName",  jwt.getClaimAsString("name")
        ));
    }

    @GetMapping("/me/profile")
    public ResponseEntity<Map<String, Object>> getMyProfile(
            JwtAuthenticationToken authentication
    ) {
        String userId = authentication.getToken().getSubject();
        int score = reportService.calculateScore(userId);
        return ResponseEntity.ok(authService.getUserProfile(userId, score));
    }

    // N'importe quel user peut sauvegarder la position de son service
    @PatchMapping("/me/location")
    public ResponseEntity<Map<String, String>> updateMyLocation(
            JwtAuthenticationToken authentication,
            @RequestBody Map<String, String> body
    ) {
        String userId    = authentication.getToken().getSubject();
        String latitude  = body.get("latitude");
        String longitude = body.get("longitude");

        if (latitude == null || longitude == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "latitude et longitude requis"));
        }
        authService.updateServiceLocation(userId, latitude, longitude);
        return ResponseEntity.ok(Map.of("message", "Position mise à jour"));
    }

    // Tous les users avec leur position pour la map
    @GetMapping("/users/locations")
    public ResponseEntity<List<Map<String, Object>>> getAllUsersWithLocation() {
        return ResponseEntity.ok(authService.getAllUsersWithLocation());
    }

    @GetMapping("/auth/users/{userId}")
    public ResponseEntity<Map<String, String>> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }
}