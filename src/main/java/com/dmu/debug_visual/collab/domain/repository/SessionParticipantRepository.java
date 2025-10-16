package com.dmu.debug_visual.collab.domain.repository;

import com.dmu.debug_visual.collab.domain.entity.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, Long> {
    // ✨ 세션 ID와 유저 ID로 참여 정보를 찾는 메소드
    Optional<SessionParticipant> findByCodeSession_SessionIdAndUser_UserId(String sessionId, String userId);

    // ✨ 강퇴 기능을 위해 특정 방의 모든 세션에서 특정 유저를 삭제하는 메소드
    @Modifying
    @Query("DELETE FROM SessionParticipant sp WHERE sp.codeSession.room.roomId = :roomId AND sp.user.userId = :userId")
    void deleteAllByRoomIdAndUserId(@Param("roomId") String roomId, @Param("userId") String userId);
}