package com.ditix.backend.Meeting.Repository;

import com.ditix.backend.Meeting.Model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    // Meetings créés par le user
    List<Meeting> findByCreatedByUserId(String userId);

    // Meetings où le user est participant
    @Query("SELECT m FROM Meeting m WHERE :userId MEMBER OF m.participantIds")
    List<Meeting> findByParticipant(String userId);
}
