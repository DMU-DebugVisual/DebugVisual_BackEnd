package com.dmu.debug_visual.config;

import com.dmu.debug_visual.collab.domain.entity.CodeSession;
import com.dmu.debug_visual.collab.domain.repository.CodeSessionRepository;
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

                Authentication authentication = (Authentication) userPrincipal;
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUsername();

                webSocketRoomService.addParticipant(roomId, userId);

                Map<String, Object> sessionAttributes = Objects.requireNonNull(headerAccessor.getSessionAttributes());
                sessionAttributes.put("roomId", roomId);
                sessionAttributes.put("userId", userId);

                // 만약 구독 주소에 "/session/"이 포함되어 있다면, 세션 참여자로도 등록합니다.
                if (destination.contains("/session/")) {
                    String sessionId = destination.split("/")[5];
                    sessionAttributes.put("sessionId", sessionId); // 퇴장 시 사용하기 위해 세션 ID 저장
                    webSocketRoomService.addSessionParticipant(sessionId, userId);
                    log.info("User {} joined session {}", userId, sessionId);
                }

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

                webSocketRoomService.removeParticipant(roomId, userId);

                // --- 세션 자동 비활성화 로직 ---
                if (sessionId != null) {
                    webSocketRoomService.removeSessionParticipant(sessionId, userId);

                    // 방금 나간 사람이 마지막 참여자였는지 확인
                    if (webSocketRoomService.isSessionEmpty(sessionId)) {
                        log.info("Last user left session {}. Deactivating session.", sessionId);

                        // DB에서 세션을 찾아 상태를 INACTIVE로 변경
                        codeSessionRepository.findBySessionId(sessionId).ifPresent(session -> {
                            session.updateStatus(CodeSession.SessionStatus.INACTIVE);
                            log.info("Session {} status updated to INACTIVE in DB.", sessionId);
                        });
                    }
                }

                // 변경된 방 상태(참여자 감소)를 모두에게 알림
                roomService.broadcastRoomState(roomId);
            }
        }
    }
}