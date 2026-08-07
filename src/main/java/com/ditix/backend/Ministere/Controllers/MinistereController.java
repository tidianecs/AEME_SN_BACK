package com.ditix.backend.Ministere.Controllers;

import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Services.MinistereService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ministeres")
public class MinistereController {

    private final MinistereService ministereService;

    public MinistereController(MinistereService ministereService) {
        this.ministereService = ministereService;
    }

    @GetMapping
    public ResponseEntity<List<Ministere>> getAllMinisteres() {
        return ResponseEntity.ok(ministereService.getAllMinisteres());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ministere> getMinistereById(@PathVariable Long id) {
        return ResponseEntity.ok(ministereService.getMinistereById(id));
    }

    @PostMapping
    public ResponseEntity<Ministere> createMinistere(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ministereService.createMinistere(body));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Ministere> updateMinistere(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(ministereService.updateMinistere(id, body));
    }
}
