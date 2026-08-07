package com.ditix.backend.Cohorte.Services;

import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Repository.CohorteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CohorteService {

    private final CohorteRepository cohorteRepository;

    public CohorteService(CohorteRepository cohorteRepository) {
        this.cohorteRepository = cohorteRepository;
    }

    public List<Cohorte> getAllCohortes() {
        return cohorteRepository.findAll();
    }

    public Cohorte getCohorteById(Long id) {
        return cohorteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cohorte non trouvée"));
    }

    public Cohorte createCohorte(Map<String, String> body) {
        Cohorte cohorte = new Cohorte();
        cohorte.setCode(body.get("code"));
        cohorte.setNom(body.get("nom"));
        if (body.containsKey("actif")) {
            cohorte.setActif(Boolean.parseBoolean(body.get("actif")));
        }
        return cohorteRepository.save(cohorte);
    }

    public Cohorte updateCohorte(Long id, Map<String, String> body) {
        Cohorte cohorte = getCohorteById(id);
        if (body.containsKey("code")) cohorte.setCode(body.get("code"));
        if (body.containsKey("nom")) cohorte.setNom(body.get("nom"));
        if (body.containsKey("actif")) cohorte.setActif(Boolean.parseBoolean(body.get("actif")));

        return cohorteRepository.save(cohorte);
    }
}
