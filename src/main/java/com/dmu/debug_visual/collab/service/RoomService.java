package com.dmu.debug_visual.collab.service;

import com.dmu.debug_visual.collab.domain.entity.CodeSession;
import com.dmu.debug_visual.collab.domain.repository.CodeSessionRepository;
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

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final CodeSessionRepository codeSessionRepository;

    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request, String ownerUserId) {
        // 1. 방을 생성할 유저(방장) 정보를 DB에서 조회
        User owner = userRepository.findByUserId(ownerUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + ownerUserId));

        // 2. 새로운 Room 엔티티 생성 및 저장
        Room newRoom = Room.builder()
                .name(request.getRoomName())
                .owner(owner)
                .build();
        roomRepository.save(newRoom);

        // 3. 방장(Owner)을 첫 참여자로 등록 (권한은 READ_WRITE)
        RoomParticipant ownerParticipant = RoomParticipant.builder()
                .room(newRoom)
                .user(owner)
                .permission(RoomParticipant.Permission.READ_WRITE)
                .build();
        roomParticipantRepository.save(ownerParticipant);

        // 4. 기본 코드 세션("main") 생성
        CodeSession defaultSession = CodeSession.builder()
                .sessionName("main") // 기본 세션 이름
                .room(newRoom)
                .build();
        codeSessionRepository.save(defaultSession);

        // 5. 응답 DTO를 만들어 반환
        return RoomResponse.builder()
                .roomId(newRoom.getRoomId())
                .roomName(newRoom.getName())
                .ownerId(owner.getUserId())
                .defaultSessionId(defaultSession.getSessionId())
                .build();
    }

    @Transactional
    public SessionResponse createCodeSessionInRoom(String roomId, CreateSessionRequest request, String creatorUserId) {
        // 1. roomId로 해당 방을 DB에서 조회
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + roomId));

        // 2. (보안) 요청자가 해당 방의 참여자인지 확인
        roomParticipantRepository.findByRoomAndUser_UserId(room, creatorUserId)
                .orElseThrow(() -> new IllegalStateException("You are not a participant of this room."));

        // 3. 새로운 CodeSession 엔티티 생성
        CodeSession newSession = CodeSession.builder()
                .sessionName(request.getSessionName())
                .room(room)
                .build();
        codeSessionRepository.save(newSession);

        // 4. 응답 DTO를 만들어 반환
        return SessionResponse.builder()
                .sessionId(newSession.getSessionId())
                .sessionName(newSession.getSessionName())
                .build();
    }
}
