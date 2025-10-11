package com.dmu.debug_visual.config;

import com.dmu.debug_visual.collab.domain.entity.Room;
import com.dmu.debug_visual.collab.domain.repository.RoomRepository;
import com.dmu.debug_visual.collab.rest.dto.SystemMessage;
import com.dmu.debug_visual.collab.service.WebSocketRoomService;
import com.dmu.debug_visual.collab.websocket.dto.WebSocketRoom;
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
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final WebSocketRoomService webSocketRoomService;
    private final RoomRepository roomRepository; // ✨ DB 조회를 위해 RoomRepository 주입

    @EventListener
    @Transactional
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal userPrincipal = headerAccessor.getUser();

        if (userPrincipal == null) {
            log.warn("Cannot process subscribe: user not authenticated.");
            return;
        }

        String destination = headerAccessor.getDestination();
        if (destination != null && destination.contains("/topic/room/")) {
            try {
                String roomId = destination.split("/")[3];

                // ✨ 1. 메모리에 활성화된 방이 있는지 확인
                WebSocketRoom activeRoom = webSocketRoomService.findActiveRoomById(roomId);
                if (activeRoom == null) {
                    // ✨ 2. 없다면, DB에서 방 정보를 가져와서 "파티 시작" (메모리에 활성화)
                    log.info("Activating room {} in memory.", roomId);
                    Room dbRoom = roomRepository.findByRoomId(roomId)
                            .orElseThrow(() -> new RuntimeException("Subscribing to a non-existent room: " + roomId));
                    // WebSocket 서비스에 방을 활성화하도록 요청 (방장 정보와 함께)
                    webSocketRoomService.activateRoom(roomId, dbRoom.getOwner().getUserId());
                }

                // ✨ 3. 이제 사용자를 활성화된 방에 참여시킴
                Authentication authentication = (Authentication) userPrincipal;
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUsername();
                String userName = userDetails.getUser().getName();

                webSocketRoomService.addParticipant(roomId, userId);

                // ✨ 4. 퇴장 이벤트를 위해 세션에 정보 저장
                Map<String, Object> sessionAttributes = Objects.requireNonNull(headerAccessor.getSessionAttributes());
                sessionAttributes.put("roomId", roomId);
                sessionAttributes.put("userName", userName);

                log.info("[입장] 사용자: {}, 방: {}", userName, roomId);
                SystemMessage systemMessage = SystemMessage.builder().content(userName + "님이 입장했습니다.").build();

                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/system", systemMessage);

            } catch (Exception e) {
                log.error("Error handling subscribe event: ", e);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        if (sessionAttributes != null) {
            String userName = (String) sessionAttributes.get("userName");
            String roomId = (String) sessionAttributes.get("roomId");

            if (userName != null && roomId != null) {
                log.info("[퇴장] 사용자: {}, 방: {}", userName, roomId);
                SystemMessage systemMessage = SystemMessage.builder().content(userName + "님이 퇴장했습니다.").build();
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/system", systemMessage);
            }
        }
    }
}