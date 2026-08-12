package com.ditix.backend.ProfilUtilisateur.Services;

import com.ditix.backend.Chat.Services.ChatUserMembershipService;
import com.ditix.backend.ProfilUtilisateur.DTO.ActivationUtilisateurRequest;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Repository.ProfilUtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ActivationUtilisateurLocalService {

    private final ProfilUtilisateurRepository profilUtilisateurRepository;
    private final ChatUserMembershipService chatUserMembershipService;

    public ActivationUtilisateurLocalService(
            ProfilUtilisateurRepository profilUtilisateurRepository,
            ChatUserMembershipService chatUserMembershipService) {
        this.profilUtilisateurRepository = profilUtilisateurRepository;
        this.chatUserMembershipService = chatUserMembershipService;
    }

    @Transactional
    public void processLocalActivation(Long targetId, ActivationUtilisateurRequest request) {
        ProfilUtilisateur target = profilUtilisateurRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (target.getRole() == RoleUtilisateur.ADMIN && !request.getActif()) {
            profilUtilisateurRepository.findAllAdminsForUpdate();
            long activeAdminCount = profilUtilisateurRepository.findAll().stream()
                    .filter(p -> p.getRole() == RoleUtilisateur.ADMIN && Boolean.TRUE.equals(p.getActif()))
                    .count();
            if (activeAdminCount <= 1 && Boolean.TRUE.equals(target.getActif())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Impossible de désactiver le dernier administrateur actif");
            }
        }

        target.setActif(request.getActif());
        profilUtilisateurRepository.save(target);

        if (!request.getActif()) {
            chatUserMembershipService.deactivateManagedMemberships(target.getKeycloakId().toString());
        } else {
            chatUserMembershipService.syncUserMemberships(target);
        }
    }
}
