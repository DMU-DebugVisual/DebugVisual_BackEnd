package com.dmu.debug_visual.config;

import com.dmu.debug_visual.websocket.dto.SystemMessage;
import com.dmu.debug_visual.websocket.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final RoomService roomService;

    // 사용자가 특정 방을 구독할 때(입장) 호출되는 메서드
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        String destination = headerAccessor.getDestination();
        if (destination != null && destination.startsWith("/topic/room/")) {
            String roomId = destination.split("/")[3];
            String userName = (String) Objects.requireNonNull(sessionAttributes).get("userName");

            String userId = (String) sessionAttributes.get("userId");

            // ✨ 중요: 세션에 현재 사용자가 어느 방에 들어갔는지 기록합니다.
            if (userId != null) {
                roomService.addParticipant(roomId, userId);
            }

            log.info("[입장] 사용자: {}, 방: {}", userName, roomId);
            SystemMessage chatMessage = SystemMessage.builder()
                    .roomId(roomId)
                    .senderName("System")
                    .content(userName + "님이 입장했습니다.")
                    .build();


            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/system", chatMessage);
        }
    }

    // 사용자의 웹소켓 연결이 끊어졌을 때(퇴장) 호출되는 메서드
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        if (sessionAttributes != null) {
            String userName = (String) sessionAttributes.get("userName");

            // ✨ 중요: 세션에서 아까 저장해둔 roomId를 꺼냅니다.
            String roomId = (String) sessionAttributes.get("roomId");

            if (userName != null && roomId != null) {
                log.info("[퇴장] 사용자: {}, 방: {}", userName, roomId);

                SystemMessage chatMessage = SystemMessage.builder()
                        .roomId(roomId)
                        .senderName("System")
                        .content(userName + "님이 퇴장했습니다.")
                        .build();

                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/system", chatMessage);
            }
        }
    }
}