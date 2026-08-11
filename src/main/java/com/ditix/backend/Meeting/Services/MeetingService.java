package com.ditix.backend.Meeting.Services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.ditix.backend.Meeting.Repository.MeetingRepository;
import com.ditix.backend.Meeting.DTO.MeetingResponseDTO;
import com.ditix.backend.Meeting.Model.Meeting;
import com.ditix.backend.Meeting.DTO.CreateMeetingRequest;
import com.ditix.backend.Meeting.Model.MeetingStatus;
import com.ditix.backend.ProfilUtilisateur.Model.ProfilUtilisateur;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingAutorisationService autorisationService;

    public MeetingService(MeetingRepository meetingRepository, MeetingAutorisationService autorisationService) {
        this.meetingRepository = meetingRepository;
        this.autorisationService = autorisationService;
    }

    public MeetingResponseDTO createMeeting(CreateMeetingRequest request, String userId) {
        Meeting meeting = new Meeting();
        meeting.setScheduledAt(request.getScheduledAt());
        meeting.setCreatedByUserId(userId);
        meeting.setType(com.ditix.backend.Meeting.Model.MeetingType.DIRECT);
        meeting.setReferenceId(null);

        String roomId = "aeme-" + UUID.randomUUID().toString().substring(0, 8);
        meeting.setRoomId(roomId);

        List<String> participants = new ArrayList<>();
        participants.add(userId);
        if (request.getParticipantIds() != null) {
            request.getParticipantIds().stream()
                .filter(id -> !id.equals(userId))
                .forEach(participants::add);
        }
        meeting.setParticipantIds(participants);

        return new MeetingResponseDTO(meetingRepository.save(meeting));
    }

    public List<MeetingResponseDTO> getMyMeetings(ProfilUtilisateur profil) {
        List<Meeting> accessible = autorisationService.obtenirMeetingsAccessibles(profil);
        return accessible.stream()
            .map(MeetingResponseDTO::new)
            .collect(Collectors.toList());
    }

    public MeetingResponseDTO getMeetingById(Long id, ProfilUtilisateur profil) {
        Meeting meeting = meetingRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting introuvable"));

        if (!autorisationService.peutLireMeeting(meeting, profil)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        return new MeetingResponseDTO(meeting);
    }

    public MeetingResponseDTO updateStatus(Long id, String status, String userId) {
        Meeting meeting = meetingRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting introuvable"));

        if (!meeting.getCreatedByUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        meeting.setStatus(MeetingStatus.valueOf(status));
        return new MeetingResponseDTO(meetingRepository.save(meeting));
    }

    public void deleteMeeting(Long id, String userId) {
        Meeting meeting = meetingRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting introuvable"));

        if (!meeting.getCreatedByUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
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
