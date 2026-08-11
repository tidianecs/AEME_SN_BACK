package com.ditix.backend.Chat.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatGroupUserDirectory {

    private static final Logger log = LoggerFactory.getLogger(ChatGroupUserDirectory.class);

    private final ProfilUtilisateurRepository profilRepository;

    public ChatGroupUserDirectory(ProfilUtilisateurRepository profilRepository) {
        this.profilRepository = profilRepository;
    }

    public List<String> fetchGlobalMembers() {
        return profilRepository.findActiveGlobalChatMembers().stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
    }

    public List<String> fetchCohortMembers(String cohortRef) {
        try {
            return profilRepository.findActiveMembersByCohorte(Long.parseLong(cohortRef)).stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            log.error("Invalid cohort reference ID format: {}", cohortRef);
            return List.of();
        }
    }

    public List<String> fetchStructureMembers(String structureRef) {
        try {
            return profilRepository.findActiveMembersByStructure(Long.parseLong(structureRef)).stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            log.error("Invalid structure reference ID format: {}", structureRef);
            return List.of();
        }
    }

    public List<String> fetchMinistereMembers(String ministereRef) {
        try {
            return profilRepository.findActiveMembersByMinistere(Long.parseLong(ministereRef)).stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            log.error("Invalid ministere reference ID format: {}", ministereRef);
            return List.of();
        }
    }
}
