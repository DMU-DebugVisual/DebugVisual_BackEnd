package com.dmu.debug_visual.websocket.service;

import com.dmu.debug_visual.websocket.dto.Room;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {

    // 현재 생성된 모든 방의 정보를 서버 메모리에 저장합니다.
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    /**
     * 새로운 협업 방을 생성합니다.
     * @param ownerId 방을 생성하는 사용자의 ID
     * @return 생성된 방의 정보
     */
    public Room createRoom(String ownerId) {
        // 고유한 방 ID를 생성합니다.
        String roomId = UUID.randomUUID().toString();
        Room room = Room.builder()
                .roomId(roomId)
                .ownerId(ownerId)
                .build();

        rooms.put(roomId, room);
        return room;
    }

    /**
     * ID로 방을 찾습니다.
     * @param roomId 찾으려는 방의 ID
     * @return 찾아낸 방의 정보 (없으면 null)
     */
    public Room findRoomById(String roomId) {
        return rooms.get(roomId);
    }

    /**
     * 특정 사용자가 특정 방에서 쓰기 권한을 가지고 있는지 확인합니다.
     * @param roomId 확인할 방의 ID
     * @param userId 확인할 사용자의 ID
     * @return 쓰기 권한이 있으면 true
     */
    public boolean hasWritePermission(String roomId, String userId) {
        Room room = findRoomById(roomId);
        if (room == null) {
            return false;
        }

        Room.Permission permission = room.getParticipants().get(userId);
        return Room.Permission.READ_WRITE.equals(permission);
    }

    /**
     * 특정 방에 새로운 참여자를 추가합니다.
     * @param roomId 참여할 방의 ID
     * @param userId 새로운 참여자의 ID
     */
    public void addParticipant(String roomId, String userId) {
        Room room = findRoomById(roomId);
        if (room != null) {
            room.addParticipant(userId);
        }
    }
}