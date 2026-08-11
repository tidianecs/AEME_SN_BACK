package com.ditix.backend.Meeting.Services;

import com.ditix.backend.Cohorte.Model.Cohorte;
import com.ditix.backend.Cohorte.Repository.CohorteRepository;
import com.ditix.backend.Meeting.DTO.CreateManagedMeetingRequest;
import com.ditix.backend.Meeting.DTO.MeetingResponseDTO;
import com.ditix.backend.Meeting.Model.Meeting;
import com.ditix.backend.Meeting.Model.MeetingStatus;
import com.ditix.backend.Meeting.Model.MeetingType;
import com.ditix.backend.Meeting.Repository.MeetingRepository;
import com.ditix.backend.Ministere.Model.Ministere;
import com.ditix.backend.Ministere.Repository.MinistereRepository;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;
import com.ditix.backend.Structure.Model.Structure;
import com.ditix.backend.Structure.Repository.StructureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class MeetingAdminService {

    private final MeetingRepository meetingRepository;
    private final CohorteRepository cohorteRepository;
    private final StructureRepository structureRepository;
    private final MinistereRepository ministereRepository;

    public MeetingAdminService(MeetingRepository meetingRepository,
                               CohorteRepository cohorteRepository,
                               StructureRepository structureRepository,
                               MinistereRepository ministereRepository) {
        this.meetingRepository = meetingRepository;
        this.cohorteRepository = cohorteRepository;
        this.structureRepository = structureRepository;
        this.ministereRepository = ministereRepository;
    }

    private Meeting initManagedMeeting(CreateManagedMeetingRequest request, ProfilUtilisateur admin, MeetingType type, Long referenceId) {
        Meeting meeting = new Meeting();
        meeting.setScheduledAt(request.getScheduledAt());
        meeting.setCreatedByUserId(admin.getKeycloakId().toString());
        meeting.setType(type);
        meeting.setReferenceId(referenceId);

        String roomId = "aeme-" + UUID.randomUUID().toString().substring(0, 8);
        meeting.setRoomId(roomId);
        return meeting;
    }

    public MeetingResponseDTO createGlobalMeeting(CreateManagedMeetingRequest request, ProfilUtilisateur admin) {
        Meeting meeting = initManagedMeeting(request, admin, MeetingType.GLOBAL, null);
        return new MeetingResponseDTO(meetingRepository.save(meeting));
    }

    public MeetingResponseDTO createCohortMeeting(CreateManagedMeetingRequest request, ProfilUtilisateur admin) {
        if (request.getTargetId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de cohorte requis");
        }
        Cohorte cohorte = cohorteRepository.findById(request.getTargetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cohorte introuvable"));

        if (cohorte.getActif() != null && !cohorte.getActif()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "La cohorte est inactive");
        }

        Meeting meeting = initManagedMeeting(request, admin, MeetingType.COHORT, request.getTargetId());
        return new MeetingResponseDTO(meetingRepository.save(meeting));
    }

    public MeetingResponseDTO createStructureMeeting(CreateManagedMeetingRequest request, ProfilUtilisateur admin) {
        if (request.getTargetId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de structure requis");
        }
        Structure structure = structureRepository.findById(request.getTargetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Structure introuvable"));

        if (structure.getActif() != null && !structure.getActif()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "La structure est inactive");
        }

        Meeting meeting = initManagedMeeting(request, admin, MeetingType.STRUCTURE, request.getTargetId());
        return new MeetingResponseDTO(meetingRepository.save(meeting));
    }

    public MeetingResponseDTO createMinistereMeeting(CreateManagedMeetingRequest request, ProfilUtilisateur admin) {
        if (request.getTargetId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de ministère requis");
        }
        Ministere ministere = ministereRepository.findById(request.getTargetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ministère introuvable"));

        if (ministere.getActif() != null && !ministere.getActif()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Le ministère est inactif");
        }

        Meeting meeting = initManagedMeeting(request, admin, MeetingType.MINISTERE, request.getTargetId());
        return new MeetingResponseDTO(meetingRepository.save(meeting));
    }

    public MeetingResponseDTO updateStatus(Long id, String status, ProfilUtilisateur admin) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting introuvable"));

        if (meeting.getType() == MeetingType.DIRECT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le créateur peut modifier ce meeting DIRECT");
        }

        meeting.setStatus(MeetingStatus.valueOf(status));
        return new MeetingResponseDTO(meetingRepository.save(meeting));
    }

    public void deleteMeeting(Long id, ProfilUtilisateur admin) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting introuvable"));

        if (meeting.getType() == MeetingType.DIRECT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le créateur peut supprimer ce meeting DIRECT");
        }

        if (meeting.getStatus() == MeetingStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Impossible de supprimer un meeting en cours"
            );
        }

        meetingRepository.delete(meeting);
    }
}
