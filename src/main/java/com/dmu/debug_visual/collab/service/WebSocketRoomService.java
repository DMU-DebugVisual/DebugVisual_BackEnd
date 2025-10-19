package com.dmu.debug_visual.collab.service;

import com.dmu.debug_visual.collab.websocket.dto.WebSocketRoom;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketRoomService {

    // key: roomId, value: WebSocketRoom (방의 기본 정보)
    private final Map<String, WebSocketRoom> activeRooms = new ConcurrentHashMap<>();

    // key: sessionId, value: Set of userIds
    private final Map<String, Set<String>> sessionParticipants = new ConcurrentHashMap<>();

    // --- 방(Room) 관련 메소드 ---

    public WebSocketRoom activateRoom(String roomId, String ownerId) {
        // computeIfAbsent를 사용하면 if문 없이 더 간결하게 코드를 작성할 수 있습니다.
        return activeRooms.computeIfAbsent(roomId, k -> WebSocketRoom.builder()
                .roomId(k)
                .ownerId(ownerId)
                .build());
    }

    public WebSocketRoom findActiveRoomById(String roomId) {
        return activeRooms.get(roomId);
    }

    public void addParticipant(String roomId, String userId) {
        WebSocketRoom activeRoom = findActiveRoomById(roomId);
        if (activeRoom != null) {
            activeRoom.addParticipant(userId);
        }
    }

    public void removeParticipant(String roomId, String userId) {
        WebSocketRoom activeRoom = findActiveRoomById(roomId);
        if (activeRoom != null) {
            // Map에서 참여자를 제거합니다.
            activeRoom.getParticipants().remove(userId);
        }
    }

    // --- 세션(Session) 관련 메소드 ---

    /**
     * 특정 세션에 실시간 참여자를 추가합니다.
     * @param sessionId 참여할 세션 ID
     * @param userId 참여하는 사용자 ID
     */
    public void addSessionParticipant(String sessionId, String userId) {
        // computeIfAbsent를 사용하여 sessionId가 없으면 새로 Set을 만들고, 있으면 기존 Set에 userId를 추가합니다.
        sessionParticipants.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    /**
     * 특정 세션에서 실시간 참여자를 제거합니다.
     * @param sessionId 나가는 세션 ID
     * @param userId 나가는 사용자 ID
     */
    public void removeSessionParticipant(String sessionId, String userId) {
        if (sessionParticipants.containsKey(sessionId)) {
            sessionParticipants.get(sessionId).remove(userId);
        }
    }

    /**
     * 특정 세션이 비어있는지 (아무도 접속해있지 않은지) 확인합니다.
     * @param sessionId 확인할 세션 ID
     * @return 세션이 비어있거나 존재하지 않으면 true
     */
    public boolean isSessionEmpty(String sessionId) {
        // sessionId에 해당하는 참여자 목록이 없거나, 있더라도 비어있으면 true를 반환합니다.
        return !sessionParticipants.containsKey(sessionId) || sessionParticipants.get(sessionId).isEmpty();
    }
}