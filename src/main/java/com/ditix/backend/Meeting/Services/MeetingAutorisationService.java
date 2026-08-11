package com.ditix.backend.Meeting.Services;

import com.ditix.backend.Meeting.Model.Meeting;
import com.ditix.backend.Meeting.Model.MeetingType;
import com.ditix.backend.Meeting.Repository.MeetingRepository;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.ProfilUtilisateur.Model.RoleUtilisateur;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MeetingAutorisationService {

    private final MeetingRepository meetingRepository;

    public MeetingAutorisationService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    public boolean peutLireMeeting(Meeting meeting, ProfilUtilisateur profil) {
        if (profil == null || profil.getActif() == null || !profil.getActif()) {
            return false;
        }

        if (profil.getRole() == RoleUtilisateur.ADMIN) {
            return true;
        }

        if (meeting.getType() == null) {
            return false;
        }

        String keycloakId = profil.getKeycloakId().toString();

        switch (meeting.getType()) {
            case DIRECT:
                if (keycloakId.equals(meeting.getCreatedByUserId())) {
                    return true;
                }
                return meeting.getParticipantIds() != null && meeting.getParticipantIds().contains(keycloakId);

            case GLOBAL:
                return true;

            case MINISTERE:
                if (profil.getRole() == RoleUtilisateur.DAGE) {
                    return profil.getMinistere() != null && meeting.getReferenceId() != null && meeting.getReferenceId().equals(profil.getMinistere().getId());
                } else if (profil.getRole() == RoleUtilisateur.GESTIONNAIRE) {
                    return profil.getStructure() != null
                            && profil.getStructure().getMinistereV2() != null
                            && meeting.getReferenceId() != null
                            && meeting.getReferenceId().equals(profil.getStructure().getMinistereV2().getId());
                }
                return false;

            case COHORT:
                if (profil.getRole() == RoleUtilisateur.GESTIONNAIRE) {
                    return profil.getCohorte() != null && meeting.getReferenceId() != null && meeting.getReferenceId().equals(profil.getCohorte().getId());
                }
                return false;

            case STRUCTURE:
                if (profil.getRole() == RoleUtilisateur.GESTIONNAIRE) {
                    return profil.getStructure() != null && meeting.getReferenceId() != null && meeting.getReferenceId().equals(profil.getStructure().getId());
                }
                return false;

            default:
                return false;
        }
    }

    public List<Meeting> obtenirMeetingsAccessibles(ProfilUtilisateur profil) {
        if (profil == null || profil.getActif() == null || !profil.getActif()) {
            return new ArrayList<>();
        }

        Set<Meeting> accessible = new HashSet<>();

        if (profil.getRole() == RoleUtilisateur.ADMIN) {
            return meetingRepository.findAll();
        }

        // All active users see GLOBAL
        accessible.addAll(meetingRepository.findByType(MeetingType.GLOBAL));

        // Direct meetings created by user or where user is participant
        String keycloakId = profil.getKeycloakId().toString();
        accessible.addAll(meetingRepository.findByCreatedByUserId(keycloakId).stream()
                .filter(m -> m.getType() == MeetingType.DIRECT)
                .toList());
        accessible.addAll(meetingRepository.findByParticipant(keycloakId).stream()
                .filter(m -> m.getType() == MeetingType.DIRECT)
                .toList());

        if (profil.getRole() == RoleUtilisateur.DAGE) {
            if (profil.getMinistere() != null) {
                accessible.addAll(meetingRepository.findByTypeAndReferenceId(MeetingType.MINISTERE, profil.getMinistere().getId()));
            }
        } else if (profil.getRole() == RoleUtilisateur.GESTIONNAIRE) {
            if (profil.getCohorte() != null) {
                accessible.addAll(meetingRepository.findByTypeAndReferenceId(MeetingType.COHORT, profil.getCohorte().getId()));
            }
            if (profil.getStructure() != null) {
                accessible.addAll(meetingRepository.findByTypeAndReferenceId(MeetingType.STRUCTURE, profil.getStructure().getId()));
                if (profil.getStructure().getMinistereV2() != null) {
                    accessible.addAll(meetingRepository.findByTypeAndReferenceId(MeetingType.MINISTERE, profil.getStructure().getMinistereV2().getId()));
                }
            }
        }

        return new ArrayList<>(accessible);
    }
}
