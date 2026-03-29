package com.ditix.backend.Auth.Controllers;

import com.ditix.backend.Auth.Services.AuthService;
import com.ditix.backend.Report.Services.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/auth/users/{userId}")
    public ResponseEntity<Map<String, String>> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }
}