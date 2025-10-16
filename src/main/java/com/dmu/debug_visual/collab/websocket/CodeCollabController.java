package com.dmu.debug_visual.collab.websocket;

import com.dmu.debug_visual.collab.service.RoomService;
import com.dmu.debug_visual.user.UserRepository;
import com.dmu.debug_visual.collab.websocket.dto.CodeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

/**
 * 실시간 협업 관련 WebSocket 메시지만을 처리하는 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class CodeCollabController {

    private final RoomService roomService;
    private final UserRepository userRepository;
    private final SimpMessageSendingOperations messagingTemplate;

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
    public void handleCodeUpdate(
            @DestinationVariable String roomId,
            @DestinationVariable String sessionId,
            CodeMessage message) {

        // 세션 단위로 권한을 검사합니다.
        if (roomService.hasWritePermissionInSession(sessionId, message.getSenderId())) {
            // 사용자 이름을 찾아서 메시지에 추가
            userRepository.findByUserId(message.getSenderId()).ifPresent(user -> {
                message.setSenderName(user.getName());
            });

            String topic = String.format("/topic/room/%s/session/%s/code", roomId, sessionId);
            messagingTemplate.convertAndSend(topic, message);
        }
    }
}
