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

    @PatchMapping("/me/profile")
    public ResponseEntity<Map<String, String>> updateMyProfile(
            JwtAuthenticationToken authentication,
            @RequestBody Map<String, String> body
    ) {
        String userId = authentication.getToken().getSubject();
        authService.updateUserProfile(userId, body);
        return ResponseEntity.ok(Map.of("message", "Profil mis à jour"));
    }

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

    @GetMapping("/users/locations")
    public ResponseEntity<List<Map<String, Object>>> getAllUsersWithLocation() {
        return ResponseEntity.ok(authService.getAllUsersWithLocation());
    }

    @GetMapping("/auth/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    // @GetMapping("/test-sentry")
    // public ResponseEntity<String> testSentry() {
    //     throw new RuntimeException("Test Sentry AEME — ceci est une erreur de test");
    // }

    @GetMapping("/geocode/search")
    public ResponseEntity<String> geocodeSearch(
            @RequestParam String q,
            JwtAuthenticationToken authentication
    ) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?q="
                    + java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8)
                    + "&format=json&limit=5&countrycodes=sn&accept-language=fr";

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("User-Agent", "AEME-Platform/1.0")
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(
                    request,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
            );
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(response.body());
        } catch (Exception e) {
            return ResponseEntity.ok().body("[]");
        }
    }
}