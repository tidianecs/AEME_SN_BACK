package com.ditix.backend.Structure.Services;

import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.DTO.StructureDTO;
import com.ditix.backend.Structure.Repository.StructureRepository;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import com.ditix.backend.Ministere.Model.Ministere;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StructureService {

    private final StructureRepository structureRepository;
    private final MinistereRepository ministereRepository;

    public StructureService(StructureRepository structureRepository, MinistereRepository ministereRepository) {
        this.structureRepository = structureRepository;
        this.ministereRepository = ministereRepository;
    }

    public List<StructureDTO> getAllStructures() {
        return structureRepository.findAll().stream().map(StructureDTO::new).toList();
    }

    public StructureDTO getStructureById(Long id) {
        return new StructureDTO(getStructureEntityById(id));
    }

    private Structure getStructureEntityById(Long id) {
        return structureRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Structure introuvable"));
    }

    public StructureDTO createStructure(Map<String, String> body) {
        Structure structure = new Structure();
        structure.setName(body.get("name"));
        structure.setLatitude(body.get("latitude"));
        structure.setLongitude(body.get("longitude"));
        structure.setMinistere(body.getOrDefault("ministere", ""));
        structure.setRegion(body.getOrDefault("region", ""));
        structure.setZone(body.getOrDefault("zone", ""));

        // V2 fields
        if (body.containsKey("code")) structure.setCode(body.get("code"));
        if (body.containsKey("ministereId")) {
            Long ministereId = Long.parseLong(body.get("ministereId"));
            Ministere ministere = ministereRepository.findById(ministereId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ministère invalide"));
            structure.setMinistereV2(ministere);
        }
        if (body.containsKey("latitudeV2")) structure.setLatitudeV2(Double.parseDouble(body.get("latitudeV2")));
        if (body.containsKey("longitudeV2")) structure.setLongitudeV2(Double.parseDouble(body.get("longitudeV2")));
        if (body.containsKey("departement")) structure.setDepartement(body.get("departement"));
        if (body.containsKey("commune")) structure.setCommune(body.get("commune"));
        if (body.containsKey("adresse")) structure.setAdresse(body.get("adresse"));
        if (body.containsKey("categorie")) structure.setCategorie(body.get("categorie"));
        if (body.containsKey("actif")) structure.setActif(Boolean.parseBoolean(body.get("actif")));

        return new StructureDTO(structureRepository.save(structure));
    }

    public StructureDTO updateStructure(Long id, Map<String, String> body) {
        Structure structure = getStructureEntityById(id);
        if (body.containsKey("name"))      structure.setName(body.get("name"));
        if (body.containsKey("latitude"))  structure.setLatitude(body.get("latitude"));
        if (body.containsKey("longitude")) structure.setLongitude(body.get("longitude"));
        if (body.containsKey("ministere")) structure.setMinistere(body.get("ministere"));
        if (body.containsKey("region"))    structure.setRegion(body.get("region"));
        if (body.containsKey("zone"))      structure.setZone(body.get("zone"));

        // V2 fields
        if (body.containsKey("code")) structure.setCode(body.get("code"));
        if (body.containsKey("ministereId")) {
            if (body.get("ministereId") == null || body.get("ministereId").isEmpty()) {
                structure.setMinistereV2(null);
            } else {
                Long ministereId = Long.parseLong(body.get("ministereId"));
                Ministere ministere = ministereRepository.findById(ministereId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ministère invalide"));
                structure.setMinistereV2(ministere);
            }
        }
        if (body.containsKey("latitudeV2")) structure.setLatitudeV2(body.get("latitudeV2") != null ? Double.parseDouble(body.get("latitudeV2")) : null);
        if (body.containsKey("longitudeV2")) structure.setLongitudeV2(body.get("longitudeV2") != null ? Double.parseDouble(body.get("longitudeV2")) : null);
        if (body.containsKey("departement")) structure.setDepartement(body.get("departement"));
        if (body.containsKey("commune")) structure.setCommune(body.get("commune"));
        if (body.containsKey("adresse")) structure.setAdresse(body.get("adresse"));
        if (body.containsKey("categorie")) structure.setCategorie(body.get("categorie"));
        if (body.containsKey("actif")) structure.setActif(Boolean.parseBoolean(body.get("actif")));

        return new StructureDTO(structureRepository.save(structure));
    }

    public void deleteStructure(Long id) {
        structureRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Structure introuvable"));
        structureRepository.deleteById(id);
    }
}