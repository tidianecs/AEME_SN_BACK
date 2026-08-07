package com.ditix.backend.Cohorte.Controllers;

import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Services.CohorteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cohortes")
public class CohorteController {

    private final CohorteService cohorteService;

    public CohorteController(CohorteService cohorteService) {
        this.cohorteService = cohorteService;
    }

    @GetMapping
    public ResponseEntity<List<Cohorte>> getAllCohortes() {
        return ResponseEntity.ok(cohorteService.getAllCohortes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cohorte> getCohorteById(@PathVariable Long id) {
        return ResponseEntity.ok(cohorteService.getCohorteById(id));
    }

    @PostMapping
    public ResponseEntity<Cohorte> createCohorte(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(cohorteService.createCohorte(body));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Cohorte> updateCohorte(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(cohorteService.updateCohorte(id, body));
    }
}
