package com.dmu.debug_visual.websocket.controller;


import com.dmu.debug_visual.security.CustomUserDetails;
import com.dmu.debug_visual.websocket.dto.Room;
import com.dmu.debug_visual.websocket.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/collab-rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * 새로운 협업 방을 생성합니다.
     * @param userDetails 현재 로그인한 사용자의 정보
     * @return 생성된 방의 정보 (roomId 포함)
     */
    @PostMapping
    public ResponseEntity<Room> createRoom(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 로그인한 사용자를 방의 소유자(owner)로 설정합니다.
        String ownerId = userDetails.getUsername();

        // RoomService를 통해 새로운 방을 생성합니다.
        Room newRoom = roomService.createRoom(ownerId);

        // 생성된 방의 정보를 클라이언트에게 반환합니다.
        return ResponseEntity.ok(newRoom);
    }
}