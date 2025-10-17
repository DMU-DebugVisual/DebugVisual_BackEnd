package com.dmu.debug_visual.config;

import com.dmu.debug_visual.collab.domain.entity.Room;
import com.dmu.debug_visual.collab.domain.repository.RoomRepository;
import com.dmu.debug_visual.collab.rest.dto.ParticipantInfo;
import com.dmu.debug_visual.collab.rest.dto.RoomStateUpdate;
import com.dmu.debug_visual.collab.service.WebSocketRoomService;
import com.dmu.debug_visual.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final WebSocketRoomService webSocketRoomService;
    private final RoomRepository roomRepository;

    @EventListener
    @Transactional
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal userPrincipal = headerAccessor.getUser();
        if (userPrincipal == null) return;

        String destination = headerAccessor.getDestination();
        if (destination != null && destination.contains("/topic/room/")) {
            try {
                String roomId = destination.split("/")[3];

                // --- 사용자 정보 가져오기 및 메모리에 사용자 추가 ---
                Authentication authentication = (Authentication) userPrincipal;
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUsername();
                webSocketRoomService.addParticipant(roomId, userId);

                // --- 퇴장 이벤트를 위해 세션에 정보 저장 ---
                Map<String, Object> sessionAttributes = Objects.requireNonNull(headerAccessor.getSessionAttributes());
                sessionAttributes.put("roomId", roomId);
                sessionAttributes.put("userId", userId);

                // --- 방 전체에 최신 상태 브로드캐스팅 ---
                broadcastRoomState(roomId);

            } catch (Exception e) {
                log.error("Error handling subscribe event: ", e);
            }
        }
    }

    @EventListener
    @Transactional
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        if (sessionAttributes != null) {
            String roomId = (String) sessionAttributes.get("roomId");
            String userId = (String) sessionAttributes.get("userId");

            if (roomId != null && userId != null) {
                log.info("[퇴장] 사용자: {}, 방: {}", userId, roomId);

                // 메모리에서 사용자 제거 (WebSocketRoomService에 removeParticipant 메소드 필요)
                webSocketRoomService.removeParticipant(roomId, userId);

                // --- 방 전체에 최신 상태 브로드캐스팅 ---
                broadcastRoomState(roomId);
            }
        }
    }

    /**
     * 특정 방의 최신 상태(방 이름, 방장, 참여자 목록)를 조회하여
     * 해당 방의 시스템 채널로 브로드캐스팅하는 헬퍼 메소드
     */
    private void broadcastRoomState(String roomId) {
        Room dbRoom = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found during state broadcast: " + roomId));

        // 1. 방장 정보 DTO 생성
        ParticipantInfo ownerInfo = ParticipantInfo.builder()
                .userId(dbRoom.getOwner().getUserId())
                .userName(dbRoom.getOwner().getName())
                .build();

        // 2. 참여자(방장 제외) 목록 DTO 생성
        List<ParticipantInfo> participantInfos = dbRoom.getParticipants().stream()
                .filter(p -> !p.getUser().getUserId().equals(dbRoom.getOwner().getUserId()))
                .map(p -> ParticipantInfo.builder()
                        .userId(p.getUser().getUserId())
                        .userName(p.getUser().getName())
                        .build())
                .collect(Collectors.toList());

        // 3. 최종 업데이트 DTO 생성
        RoomStateUpdate roomStateUpdate = RoomStateUpdate.builder()
                .roomName(dbRoom.getName())
                .owner(ownerInfo)
                .participants(participantInfos)
                .build();

        // 4. 시스템 채널로 브로드캐스팅
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/system", roomStateUpdate);
        log.info("Broadcasted room state update for room: {}", roomId);
    }
}