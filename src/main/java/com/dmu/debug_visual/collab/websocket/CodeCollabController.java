package com.dmu.debug_visual.collab.websocket;

import com.dmu.debug_visual.user.User;
import com.dmu.debug_visual.collab.rest.dto.SystemMessage;
import com.dmu.debug_visual.user.UserRepository;
import com.dmu.debug_visual.collab.websocket.dto.CodeMessage;
import com.dmu.debug_visual.collab.rest.dto.PermissionChangeMessage;
import com.dmu.debug_visual.collab.websocket.dto.WebSocketRoom;
import com.dmu.debug_visual.collab.service.WebSocketRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

/**
 * 실시간 협업 관련 WebSocket 메시지를 처리하는 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class CodeCollabController {

    private final WebSocketRoomService webSocketRoomService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final UserRepository userRepository;

    /**
     * 특정 코드 세션 내에서 발생하는 코드 수정 이벤트를 처리합니다.
     * 클라이언트는 이 주소('/app/room/{roomId}/session/{sessionId}/code-update')로 CodeMessage를 발행(publish)합니다.
     * 서버는 해당 세션을 구독 중인 모든 클라이언트에게 변경된 코드를 브로드캐스팅합니다.
     *
     * @param roomId    현재 방의 고유 ID
     * @param sessionId 현재 코드 세션의 고유 ID
     * @param message   전송된 코드 정보 (senderId, content 등)
     */
    @MessageMapping("/room/{roomId}/session/{sessionId}/code-update")
    public void handleCodeUpdate(@DestinationVariable String roomId, @DestinationVariable String sessionId, CodeMessage message) {
        if (webSocketRoomService.hasWritePermission(roomId, message.getSenderId())) {
            userRepository.findByUserId(message.getSenderId()).ifPresent(user -> {
                message.setSenderName(user.getName());
            });

            String topic = String.format("/topic/room/%s/session/%s/code", roomId, sessionId);
            messagingTemplate.convertAndSend(topic, message);
        }
    }

    /**
     * 방장이 특정 참여자에게 쓰기 권한을 부여하는 이벤트를 처리합니다.
     * 클라이언트는 이 주소('/app/room/{roomId}/grant-permission')로 PermissionChangeMessage를 발행(publish)합니다.
     * 서버는 해당 방을 구독 중인 모든 클라이언트에게 권한 변경 사실을 브로드캐스팅합니다.
     *
     * @param roomId  현재 방의 고유 ID
     * @param message 권한 변경 정보 (senderId, targetUserId 등)
     */
    @MessageMapping("/room/{roomId}/grant-permission")
    public void handlePermissionGrant(@DestinationVariable String roomId, PermissionChangeMessage message) {
        WebSocketRoom webSocketRoom = webSocketRoomService.findActiveRoomById(roomId);
        if (webSocketRoom != null && webSocketRoom.getOwnerId().equals(message.getSenderId())) {
            webSocketRoom.grantWritePermission(message.getTargetUserId());

            userRepository.findByUserId(message.getSenderId()).ifPresent(sender -> message.setSenderName(sender.getName()));
            userRepository.findByUserId(message.getTargetUserId()).ifPresent(target -> message.setTargetUserName(target.getName()));

            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/permission", message);
        }
    }

    /**
     * 방장이 특정 참여자의 쓰기 권한을 회수하는 이벤트를 처리합니다.
     * @param roomId 현재 방의 고유 ID
     * @param message 권한 변경 정보 (senderId, targetUserId)
     */
    @MessageMapping("/room/{roomId}/revoke-permission")
    public void handlePermissionRevoke(@DestinationVariable String roomId, PermissionChangeMessage message) {
        WebSocketRoom webSocketRoom = webSocketRoomService.findActiveRoomById(roomId);
        // 방장(owner)만 권한을 회수할 수 있습니다.
        if (webSocketRoom != null && webSocketRoom.getOwnerId().equals(message.getSenderId())) {
            // 1. DTO의 권한 회수 메소드 호출
            webSocketRoom.revokeWritePermission(message.getTargetUserId());

            // 2. 알림 메시지를 위한 이름 조회
            String senderName = userRepository.findByUserId(message.getSenderId()).map(User::getName).orElse("방장");
            String targetUserName = userRepository.findByUserId(message.getTargetUserId()).map(User::getName).orElse("참여자");

            // 3. 시스템 채널로 권한 회수 사실을 모두에게 알림
            SystemMessage systemMessage = SystemMessage.builder()
                    .content(senderName + "님이 " + targetUserName + "님의 쓰기 권한을 회수했습니다.")
                    .build();

            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/system", systemMessage);
        }
    }
}