package com.dmu.debug_visual.collab.domain.repository;

import com.dmu.debug_visual.collab.domain.entity.Room;
import com.dmu.debug_visual.collab.domain.entity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    Optional<RoomParticipant> findByRoomAndUser_UserId(Room room, String userId);
}