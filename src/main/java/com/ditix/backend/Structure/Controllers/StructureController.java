package com.ditix.backend.Structure.Controllers;

import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Services.StructureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/structures")
public class StructureController {

    private final StructureService structureService;

    public StructureController(StructureService structureService) {
        this.structureService = structureService;
    }

    // Public — tout user authentifié peut voir la liste
    @GetMapping
    public ResponseEntity<List<Structure>> getAllStructures() {
        return ResponseEntity.ok(structureService.getAllStructures());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Structure> getStructureById(@PathVariable Long id) {
        return ResponseEntity.ok(structureService.getStructureById(id));
    }

    // Admin uniquement
    @PostMapping
    public ResponseEntity<Structure> createStructure(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(structureService.createStructure(body));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Structure> updateStructure(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(structureService.updateStructure(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteStructure(@PathVariable Long id) {
        structureService.deleteStructure(id);
        return ResponseEntity.ok(Map.of("message", "Structure supprimée"));
    }
}