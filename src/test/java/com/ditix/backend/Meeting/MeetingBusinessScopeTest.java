package com.ditix.backend.Meeting;

import com.ditix.backend.Meeting.Model.Meeting;
import com.ditix.backend.Meeting.Model.MeetingType;
import com.ditix.backend.Meeting.Model.MeetingStatus;
import com.ditix.backend.Meeting.Repository.MeetingRepository;
import com.ditix.backend.Meeting.Services.MeetingService;
import com.ditix.backend.Meeting.DTO.CreateMeetingRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MeetingBusinessScopeTest {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingService meetingService;

    @Test
    public void testDirectPersists() {
        Meeting m = new Meeting();
        m.setType(MeetingType.DIRECT);
        m.setScheduledAt(LocalDateTime.now().plusDays(1));
        m.setCreatedByUserId("user-1");
        m.setRoomId("room-1");

        Meeting saved = meetingRepository.saveAndFlush(m);
        assertNotNull(saved.getId());
        assertNull(saved.getReferenceId());
    }

    @Test
    public void testGlobalPersists() {
        Meeting m = new Meeting();
        m.setType(MeetingType.GLOBAL);
        m.setScheduledAt(LocalDateTime.now().plusDays(1));
        m.setCreatedByUserId("user-1");
        m.setRoomId("room-global-1");

        Meeting saved = meetingRepository.saveAndFlush(m);
        assertNotNull(saved.getId());
        assertNull(saved.getReferenceId());
    }

    @Test
    public void testCohortPersists() {
        Meeting m = new Meeting();
        m.setType(MeetingType.COHORT);
        m.setReferenceId(10L);
        m.setScheduledAt(LocalDateTime.now().plusDays(1));
        m.setCreatedByUserId("user-1");
        m.setRoomId("room-cohort-1");

        Meeting saved = meetingRepository.saveAndFlush(m);
        assertNotNull(saved.getId());
        assertEquals(10L, saved.getReferenceId());
    }

    @Test
    public void testStructurePersists() {
        Meeting m = new Meeting();
        m.setType(MeetingType.STRUCTURE);
        m.setReferenceId(20L);
        m.setScheduledAt(LocalDateTime.now().plusDays(1));
        m.setCreatedByUserId("user-1");
        m.setRoomId("room-structure-1");

        Meeting saved = meetingRepository.saveAndFlush(m);
        assertNotNull(saved.getId());
        assertEquals(20L, saved.getReferenceId());
    }

    @Test
    public void testMinisterePersists() {
        Meeting m = new Meeting();
        m.setType(MeetingType.MINISTERE);
        m.setReferenceId(30L);
        m.setScheduledAt(LocalDateTime.now().plusDays(1));
        m.setCreatedByUserId("user-1");
        m.setRoomId("room-ministere-1");

        Meeting saved = meetingRepository.saveAndFlush(m);
        assertNotNull(saved.getId());
        assertEquals(30L, saved.getReferenceId());
    }

    @Test
    public void testInvalidReferenceCombinations() {
        // DIRECT + non-null referenceId
        Meeting m1 = new Meeting();
        m1.setType(MeetingType.DIRECT);
        m1.setReferenceId(1L);
        m1.setScheduledAt(LocalDateTime.now().plusDays(1));
        m1.setCreatedByUserId("user-1");
        m1.setRoomId("err-1");
        assertThrows(Exception.class, () -> meetingRepository.saveAndFlush(m1));

        // GLOBAL + non-null referenceId
        Meeting m2 = new Meeting();
        m2.setType(MeetingType.GLOBAL);
        m2.setReferenceId(1L);
        m2.setScheduledAt(LocalDateTime.now().plusDays(1));
        m2.setCreatedByUserId("user-1");
        m2.setRoomId("err-2");
        assertThrows(Exception.class, () -> meetingRepository.saveAndFlush(m2));

        // COHORT + null referenceId
        Meeting m3 = new Meeting();
        m3.setType(MeetingType.COHORT);
        m3.setScheduledAt(LocalDateTime.now().plusDays(1));
        m3.setCreatedByUserId("user-1");
        m3.setRoomId("err-3");
        assertThrows(Exception.class, () -> meetingRepository.saveAndFlush(m3));

        // STRUCTURE + null referenceId
        Meeting m4 = new Meeting();
        m4.setType(MeetingType.STRUCTURE);
        m4.setScheduledAt(LocalDateTime.now().plusDays(1));
        m4.setCreatedByUserId("user-1");
        m4.setRoomId("err-4");
        assertThrows(Exception.class, () -> meetingRepository.saveAndFlush(m4));

        // MINISTERE + null referenceId
        Meeting m5 = new Meeting();
        m5.setType(MeetingType.MINISTERE);
        m5.setScheduledAt(LocalDateTime.now().plusDays(1));
        m5.setCreatedByUserId("user-1");
        m5.setRoomId("err-5");
        assertThrows(Exception.class, () -> meetingRepository.saveAndFlush(m5));
    }

    @Test
    public void testV1CreateForcesDirect() {
        CreateMeetingRequest req = new CreateMeetingRequest();
        req.setScheduledAt(LocalDateTime.now().plusDays(2));
        req.setParticipantIds(List.of("00000000-0000-0000-0000-000000000002"));

        var response = meetingService.createMeeting(req, "00000000-0000-0000-0000-000000000001");
        assertNotNull(response.getId());

        Meeting m = meetingRepository.findById(response.getId()).orElseThrow();
        assertEquals(MeetingType.DIRECT, m.getType());
        assertNull(m.getReferenceId());

        // creator is correct
        assertEquals("00000000-0000-0000-0000-000000000001", m.getCreatedByUserId());
        // participants: creator + user-2
        assertTrue(m.getParticipantIds().contains("00000000-0000-0000-0000-000000000001"));
        assertTrue(m.getParticipantIds().contains("00000000-0000-0000-0000-000000000002"));
    }
}
