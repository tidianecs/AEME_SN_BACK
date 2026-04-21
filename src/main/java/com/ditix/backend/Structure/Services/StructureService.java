package com.ditix.backend.Structure.Services;

import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class StructureService {

    private final StructureRepository structureRepository;

    public StructureService(StructureRepository structureRepository) {
        this.structureRepository = structureRepository;
    }

    public List<Structure> getAllStructures() {
        return structureRepository.findAll();
    }

    public Structure getStructureById(Long id) {
        return structureRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Structure introuvable"));
    }

    public Structure createStructure(Map<String, String> body) {
        Structure structure = new Structure();
        structure.setName(body.get("name"));
        structure.setLatitude(body.get("latitude"));
        structure.setLongitude(body.get("longitude"));
        structure.setMinistere(body.getOrDefault("ministere", ""));
        structure.setRegion(body.getOrDefault("region", ""));
        structure.setZone(body.getOrDefault("zone", ""));
        return structureRepository.save(structure);
    }

    public Structure updateStructure(Long id, Map<String, String> body) {
        Structure structure = getStructureById(id);
        if (body.containsKey("name"))      structure.setName(body.get("name"));
        if (body.containsKey("latitude"))  structure.setLatitude(body.get("latitude"));
        if (body.containsKey("longitude")) structure.setLongitude(body.get("longitude"));
        if (body.containsKey("ministere")) structure.setMinistere(body.get("ministere"));
        if (body.containsKey("region"))    structure.setRegion(body.get("region"));
        if (body.containsKey("zone"))      structure.setZone(body.get("zone"));
        return structureRepository.save(structure);
    }

    public void deleteStructure(Long id) {
        structureRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Structure introuvable"));
        structureRepository.deleteById(id);
    }
}