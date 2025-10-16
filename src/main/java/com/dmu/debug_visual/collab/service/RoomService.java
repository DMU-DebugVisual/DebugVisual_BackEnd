package com.dmu.debug_visual.collab.service;

import com.dmu.debug_visual.collab.domain.entity.CodeSession;
import com.dmu.debug_visual.collab.domain.entity.CodeSession.SessionStatus;
import com.dmu.debug_visual.collab.domain.entity.SessionParticipant;
import com.dmu.debug_visual.collab.domain.repository.CodeSessionRepository;
import com.dmu.debug_visual.collab.domain.repository.SessionParticipantRepository;
import com.dmu.debug_visual.user.User;
import com.dmu.debug_visual.user.UserRepository;
import com.dmu.debug_visual.collab.domain.repository.RoomParticipantRepository;
import com.dmu.debug_visual.collab.domain.repository.RoomRepository;
import com.dmu.debug_visual.collab.rest.dto.CreateRoomRequest;
import com.dmu.debug_visual.collab.rest.dto.CreateSessionRequest;
import com.dmu.debug_visual.collab.rest.dto.RoomResponse;
import com.dmu.debug_visual.collab.rest.dto.SessionResponse;
import com.dmu.debug_visual.collab.domain.entity.Room;
import com.dmu.debug_visual.collab.domain.entity.RoomParticipant;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 협업 방과 세션의 생성, 관리, 권한 부여 등 핵심 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final CodeSessionRepository codeSessionRepository;

    // 1. 방 관리 (Room Management)
    /**
     * 새로운 협업 방을 생성하고, 생성자를 방장 및 첫 참여자로 등록합니다.
     * @param request 방 이름이 담긴 요청 DTO
     * @param ownerUserId 방을 생성하는 사용자의 ID
     * @return 생성된 방의 정보가 담긴 응답 DTO
     */
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request, String ownerUserId) {
        User owner = userRepository.findByUserId(ownerUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + ownerUserId));

        Room newRoom = Room.builder()
                .name(request.getRoomName())
                .owner(owner)
                .build();
        roomRepository.save(newRoom);

        RoomParticipant ownerParticipant = RoomParticipant.builder()
                .room(newRoom)
                .user(owner)
                .permission(RoomParticipant.Permission.READ_WRITE)
                .build();
        roomParticipantRepository.save(ownerParticipant);

        return RoomResponse.builder()
                .roomId(newRoom.getRoomId())
                .roomName(newRoom.getName())
                .ownerId(owner.getUserId())
                .build();
    }

    /**
     * 방장이 특정 참가자를 방에서 강퇴시킵니다.
     * @param roomId 대상 방의 ID
     * @param ownerId 요청을 보낸 방장의 ID
     * @param targetUserId 강퇴될 참가자의 ID
     */
    @Transactional
    public void kickParticipant(String roomId, String ownerId, String targetUserId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));

        if (!room.getOwner().getUserId().equals(ownerId)) {
            throw new IllegalStateException("Only the room owner can kick participants.");
        }
        if (ownerId.equals(targetUserId)) {
            throw new IllegalArgumentException("Owner cannot kick themselves.");
        }

        // 1. 방 참여자 목록에서 삭제
        RoomParticipant participantToRemove = roomParticipantRepository.findByRoomAndUser_UserId(room, targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found in this room."));
        roomParticipantRepository.delete(participantToRemove);

        // 2. 해당 방의 모든 세션 참여자 목록에서도 삭제
        sessionParticipantRepository.deleteAllByRoomIdAndUserId(roomId, targetUserId);
    }

    // 2. 세션 관리 (Session Management)

    /**
     * 특정 방 안에 새로운 코드 세션을 생성합니다. (방송 시작)
     * 세션 생성자는 READ_WRITE, 나머지 방 멤버는 READ_ONLY 권한을 자동으로 부여받습니다.
     * @param roomId 세션을 생성할 방의 ID
     * @param request 세션 이름이 담긴 요청 DTO
     * @param creatorUserId 세션을 생성하는 사용자의 ID
     * @return 생성된 세션의 정보가 담긴 응답 DTO
     */
    @Transactional
    public SessionResponse createCodeSessionInRoom(String roomId, CreateSessionRequest request, String creatorUserId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));
        User creator = userRepository.findByUserId(creatorUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + creatorUserId));

        CodeSession newSession = CodeSession.builder()
                .sessionName(request.getSessionName())
                .room(room)
                .build();
        codeSessionRepository.save(newSession);

        // 세션 생성자에게는 쓰기 권한 부여
        SessionParticipant creatorParticipant = SessionParticipant.builder()
                .codeSession(newSession)
                .user(creator)
                .permission(SessionParticipant.Permission.READ_WRITE)
                .build();
        sessionParticipantRepository.save(creatorParticipant);

        // 방에 있는 다른 모든 참여자에게는 읽기 전용 권한 부여
        room.getParticipants().stream()
                .map(RoomParticipant::getUser)
                .filter(user -> !user.getUserId().equals(creatorUserId))
                .forEach(participantUser -> {
                    SessionParticipant readOnlyParticipant = SessionParticipant.builder()
                            .codeSession(newSession)
                            .user(participantUser)
                            .permission(SessionParticipant.Permission.READ_ONLY)
                            .build();
                    sessionParticipantRepository.save(readOnlyParticipant);
                });

        return SessionResponse.builder()
                .sessionId(newSession.getSessionId())
                .sessionName(newSession.getSessionName())
                .build();
    }

    /**
     * 세션의 상태를 변경합니다. (방송 켜기/끄기)
     * @param sessionId 상태를 변경할 세션의 ID
     * @param userId 요청을 보낸 사용자의 ID
     * @param newStatus 변경할 새로운 상태 (ACTIVE / INACTIVE)
     */
    @Transactional
    public void updateSessionStatus(String sessionId, String userId, SessionStatus newStatus) {
        CodeSession session = findSessionAndVerifyCreator(sessionId, userId, "Only the session creator can change the status.");
        session.updateStatus(newStatus);
    }


    // 3. 세션 권한 관리 (Permission Management)

    /**
     * 특정 세션에 대한 사용자의 쓰기 권한 여부를 확인합니다.
     * @param sessionId 확인할 세션의 ID
     * @param userId 확인할 사용자의 ID
     * @return 쓰기 권한이 있으면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public boolean hasWritePermissionInSession(String sessionId, String userId) {
        return sessionParticipantRepository.findByCodeSession_SessionIdAndUser_UserId(sessionId, userId)
                .map(participant -> participant.getPermission() == SessionParticipant.Permission.READ_WRITE)
                .orElse(false);
    }

    /**
     * 세션 생성자가 다른 참여자에게 쓰기 권한을 부여합니다.
     * @param sessionId 권한을 부여할 세션의 ID
     * @param requesterId 권한을 부여하는 사용자(생성자)의 ID
     * @param targetUserId 권한을 받을 사용자의 ID
     */
    @Transactional
    public void grantWritePermissionInSession(String sessionId, String requesterId, String targetUserId) {
        findSessionAndVerifyCreator(sessionId, requesterId, "Only the session creator can grant permissions.");

        SessionParticipant participant = sessionParticipantRepository.findByCodeSession_SessionIdAndUser_UserId(sessionId, targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found in this session."));
        participant.updatePermission(SessionParticipant.Permission.READ_WRITE);
    }

    /**
     * 세션 생성자가 다른 참여자의 쓰기 권한을 회수합니다.
     * @param sessionId 권한을 회수할 세션의 ID
     * @param requesterId 권한을 회수하는 사용자(생성자)의 ID
     * @param targetUserId 권한을 회수당할 사용자의 ID
     */
    @Transactional
    public void revokeWritePermissionInSession(String sessionId, String requesterId, String targetUserId) {
        CodeSession session = findSessionAndVerifyCreator(sessionId, requesterId, "Only the session creator can revoke permissions.");

        if (requesterId.equals(targetUserId)) {
            throw new IllegalArgumentException("Session creator cannot revoke their own permission.");
        }

        SessionParticipant participant = sessionParticipantRepository.findByCodeSession_SessionIdAndUser_UserId(sessionId, targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found in this session."));
        participant.updatePermission(SessionParticipant.Permission.READ_ONLY);
    }

    // Private Helper Methods

    /**
     * 세션을 찾고, 요청자가 해당 세션의 생성자인지 검증하는 private 헬퍼 메소드
     */
    private CodeSession findSessionAndVerifyCreator(String sessionId, String requesterId, String errorMessage) {
        CodeSession session = codeSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));

        // 세션의 첫 번째 참여자가 생성자라는 규칙을 활용
        String creatorId = session.getParticipants().get(0).getUser().getUserId();
        if (!creatorId.equals(requesterId)) {
            throw new IllegalStateException(errorMessage);
        }
        return session;
    }
}