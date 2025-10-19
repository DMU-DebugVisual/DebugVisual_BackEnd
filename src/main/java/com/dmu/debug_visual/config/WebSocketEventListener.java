package com.dmu.debug_visual.config;

import com.dmu.debug_visual.collab.domain.entity.CodeSession;
import com.dmu.debug_visual.collab.domain.entity.Room;
import com.dmu.debug_visual.collab.domain.repository.CodeSessionRepository;
import com.dmu.debug_visual.collab.domain.repository.RoomRepository;
import com.dmu.debug_visual.collab.service.RoomService;
import com.dmu.debug_visual.collab.service.WebSocketRoomService;
import com.dmu.debug_visual.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WebSocketRoomService webSocketRoomService;
    private final RoomService roomService;
    private final RoomRepository roomRepository;
    private final CodeSessionRepository codeSessionRepository;

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

                // ✨ 1. (핵심 수정!) 메모리에 방이 없으면, DB에서 정보를 가져와 활성화시킵니다.
                if (webSocketRoomService.findActiveRoomById(roomId) == null) {
                    Room dbRoom = roomRepository.findByRoomId(roomId)
                            .orElseThrow(() -> new RuntimeException("Subscribing to a non-existent room: " + roomId));
                    webSocketRoomService.activateRoom(roomId, dbRoom.getOwner().getUserId());
                    log.info("Room {} activated in memory.", roomId);
                }

                // 2. 이제 안전하게 메모리에 실시간 사용자 추가
                Authentication authentication = (Authentication) userPrincipal;
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUsername();
                webSocketRoomService.addParticipant(roomId, userId);

                // 3. 퇴장 이벤트를 위해 세션에 정보 저장
                Map<String, Object> sessionAttributes = Objects.requireNonNull(headerAccessor.getSessionAttributes());
                sessionAttributes.put("roomId", roomId);
                sessionAttributes.put("userId", userId);

                // ... (세션 ID 저장 로직은 그대로)
                if (destination.contains("/session/")) {
                    String sessionId = destination.split("/")[5];
                    sessionAttributes.put("sessionId", sessionId);
                    webSocketRoomService.addSessionParticipant(sessionId, userId);
                    log.info("User {} joined session {}", userId, sessionId);
                }

                // 4. 변경된 상태를 모두에게 방송
                roomService.broadcastRoomState(roomId);

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
            String sessionId = (String) sessionAttributes.get("sessionId");

            if (roomId != null && userId != null) {
                log.info("[퇴장] 사용자: {}, 방: {}", userId, roomId);

                // 1. 메모리에서 방/세션 참여자 모두 제거
                webSocketRoomService.removeParticipant(roomId, userId);
                if (sessionId != null) {
                    webSocketRoomService.removeSessionParticipant(sessionId, userId);
                }

                // 2. 세션 자동 비활성화 로직
                if (sessionId != null && webSocketRoomService.isSessionEmpty(sessionId)) {
                    log.info("Last user left session {}. Deactivating session.", sessionId);
                    codeSessionRepository.findBySessionId(sessionId).ifPresent(session -> {
                        session.updateStatus(CodeSession.SessionStatus.INACTIVE);
                        log.info("Session {} status updated to INACTIVE in DB.", sessionId);
                    });
                }

                // 3. 최종적으로 변경된 상태를 모두에게 방송
                roomService.broadcastRoomState(roomId);
            }
        }
    }
}

