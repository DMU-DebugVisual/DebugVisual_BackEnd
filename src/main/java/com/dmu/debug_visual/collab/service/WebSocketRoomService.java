package com.dmu.debug_visual.collab.service;

import com.dmu.debug_visual.collab.websocket.dto.WebSocketRoom;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketRoomService {

    // 현재 생성된 모든 방의 정보를 서버 메모리에 저장합니다.
    private final Map<String, WebSocketRoom> activeRooms = new ConcurrentHashMap<>();

    /**
     * 새로운 협업 방을 생성합니다.
     * @param ownerId 방을 생성하는 사용자의 ID
     * @return 생성된 방의 정보
     */
    public WebSocketRoom activateRoom(String roomId, String ownerId) {
        if(activeRooms.containsKey(roomId)) {
            return activeRooms.get(roomId);
        }
        WebSocketRoom webSocketRoom = WebSocketRoom.builder()
                .roomId(roomId)
                .ownerId(ownerId)
                .build();
        activeRooms.put(roomId, webSocketRoom);
        return webSocketRoom;
    }

    /**
     * ID로 활성화된 방을 찾습니다.
     * @param roomId 찾으려는 방의 ID
     * @return 찾아낸 방의 정보 (없으면 null)
     */
    public WebSocketRoom findActiveRoomById(String roomId) {
        return activeRooms.get(roomId);
    }

    /**
     * 특정 사용자가 특정 방에서 쓰기 권한을 가지고 있는지 확인합니다.
     * @param roomId 확인할 방의 ID
     * @param userId 확인할 사용자의 ID
     * @return 쓰기 권한이 있으면 true
     */
    public boolean hasWritePermission(String roomId, String userId) {
        WebSocketRoom webSocketRoom = findActiveRoomById(roomId);
        if (webSocketRoom == null) {
            return false;
        }
        WebSocketRoom.Permission permission = webSocketRoom.getParticipants().get(userId);
        return WebSocketRoom.Permission.READ_WRITE.equals(permission);
    }

    /**
     * 특정 방에 새로운 참여자를 추가합니다.
     * @param roomId 참여할 방의 ID
     * @param userId 새로운 참여자의 ID
     */
    public void addParticipant(String roomId, String userId) {
        WebSocketRoom webSocketRoom = findActiveRoomById(roomId);
        if (webSocketRoom != null) {
            webSocketRoom.addParticipant(userId);
        }
    }

    public void removeParticipant(String roomId, String userId) {
        WebSocketRoom activeRoom = findActiveRoomById(roomId);
        if (activeRoom != null) {
            activeRoom.getParticipants().remove(userId);
        }
    }
}