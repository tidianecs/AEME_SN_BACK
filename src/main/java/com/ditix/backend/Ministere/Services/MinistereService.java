package com.ditix.backend.Ministere.Services;

import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MinistereService {

    private final MinistereRepository ministereRepository;

    public MinistereService(MinistereRepository ministereRepository) {
        this.ministereRepository = ministereRepository;
    }

    public List<Ministere> getAllMinisteres() {
        return ministereRepository.findAll();
    }

    public Ministere getMinistereById(Long id) {
        return ministereRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ministère non trouvé"));
    }

    public Ministere createMinistere(Map<String, String> body) {
        Ministere ministere = new Ministere();
        ministere.setCode(body.get("code"));
        ministere.setNom(body.get("nom"));
        ministere.setNomCourt(body.get("nom_court"));
        if (body.containsKey("actif")) {
            ministere.setActif(Boolean.parseBoolean(body.get("actif")));
        }
        return ministereRepository.save(ministere);
    }

    public Ministere updateMinistere(Long id, Map<String, String> body) {
        Ministere ministere = getMinistereById(id);
        if (body.containsKey("code")) ministere.setCode(body.get("code"));
        if (body.containsKey("nom")) ministere.setNom(body.get("nom"));
        if (body.containsKey("nom_court")) ministere.setNomCourt(body.get("nom_court"));
        if (body.containsKey("actif")) ministere.setActif(Boolean.parseBoolean(body.get("actif")));
        
        return ministereRepository.save(ministere);
    }
}
