package com.ditix.backend.Auth.Controllers;

import com.ditix.backend.Auth.Services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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

    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }
}