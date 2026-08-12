package com.ditix.backend.Meeting;

import com.ditix.backend.Meeting.DTO.MeetingResponseDTO;
import com.ditix.backend.Meeting.Model.Meeting;
import com.ditix.backend.Meeting.Model.MeetingType;
import com.ditix.backend.Meeting.Model.MeetingStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MeetingResponseDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void testDirectResponse() throws Exception {
        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setType(MeetingType.DIRECT);
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meeting.setScheduledAt(LocalDateTime.now());

        MeetingResponseDTO dto = new MeetingResponseDTO(meeting);

        assertEquals(MeetingType.DIRECT, dto.getType());
        assertNull(dto.getReferenceId());

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"type\":\"DIRECT\""));
        assertTrue(json.contains("\"referenceId\":null") || !json.contains("\"referenceId\""));
    }

    @Test
    void testGlobalResponse() throws Exception {
        Meeting meeting = new Meeting();
        meeting.setId(2L);
        meeting.setType(MeetingType.GLOBAL);
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meeting.setScheduledAt(LocalDateTime.now());

        MeetingResponseDTO dto = new MeetingResponseDTO(meeting);

        assertEquals(MeetingType.GLOBAL, dto.getType());
        assertNull(dto.getReferenceId());

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"type\":\"GLOBAL\""));
    }

    @Test
    void testCohortResponse() throws Exception {
        Meeting meeting = new Meeting();
        meeting.setId(3L);
        meeting.setType(MeetingType.COHORT);
        meeting.setReferenceId(100L);
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meeting.setScheduledAt(LocalDateTime.now());

        MeetingResponseDTO dto = new MeetingResponseDTO(meeting);

        assertEquals(MeetingType.COHORT, dto.getType());
        assertEquals(100L, dto.getReferenceId());

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"type\":\"COHORT\""));
        assertTrue(json.contains("\"referenceId\":100"));
    }

    @Test
    void testStructureResponse() throws Exception {
        Meeting meeting = new Meeting();
        meeting.setId(4L);
        meeting.setType(MeetingType.STRUCTURE);
        meeting.setReferenceId(200L);
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meeting.setScheduledAt(LocalDateTime.now());

        MeetingResponseDTO dto = new MeetingResponseDTO(meeting);

        assertEquals(MeetingType.STRUCTURE, dto.getType());
        assertEquals(200L, dto.getReferenceId());

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"type\":\"STRUCTURE\""));
        assertTrue(json.contains("\"referenceId\":200"));
    }

    @Test
    void testMinistereResponse() throws Exception {
        Meeting meeting = new Meeting();
        meeting.setId(5L);
        meeting.setType(MeetingType.MINISTERE);
        meeting.setReferenceId(300L);
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meeting.setScheduledAt(LocalDateTime.now());

        MeetingResponseDTO dto = new MeetingResponseDTO(meeting);

        assertEquals(MeetingType.MINISTERE, dto.getType());
        assertEquals(300L, dto.getReferenceId());

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"type\":\"MINISTERE\""));
        assertTrue(json.contains("\"referenceId\":300"));
    }
}
