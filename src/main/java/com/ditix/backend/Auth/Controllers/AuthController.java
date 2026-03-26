package com.ditix.backend.Auth.Controllers;

import com.ditix.backend.Auth.DTO.RegisterRequest;
import com.ditix.backend.Auth.Services.AuthService;
import org.springframework.http.HttpStatus;
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

    // Sign up
    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Successfully created"));
    }

    // Get the user infos
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
}