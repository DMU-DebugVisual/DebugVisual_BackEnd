package com.dmu.debug_visual.collab.domain.repository;

import com.dmu.debug_visual.collab.domain.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    // roomId (String)를 이용해 Room 엔티티를 조회하는 메소드
    Optional<Room> findByRoomId(String roomId);
}