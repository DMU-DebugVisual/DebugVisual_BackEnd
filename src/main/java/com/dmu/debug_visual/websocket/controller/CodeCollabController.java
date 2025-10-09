package com.dmu.debug_visual.websocket.controller;

import com.dmu.debug_visual.user.UserRepository; // <-- UserRepository import
import com.dmu.debug_visual.websocket.dto.CodeMessage;
import com.dmu.debug_visual.websocket.dto.PermissionChangeMessage;
import com.dmu.debug_visual.websocket.dto.Room;
import com.dmu.debug_visual.websocket.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CodeCollabController {

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository; // <-- UserRepository 주입

    // 코드 수정 메시지 처리
    @MessageMapping("/room/{roomId}/code-update")
    public void handleCodeUpdate(@DestinationVariable String roomId, CodeMessage message) {
        if (roomService.hasWritePermission(roomId, message.getSenderId())) {
            // 사용자 이름을 찾아서 메시지에 추가
            userRepository.findByUserId(message.getSenderId()).ifPresent(user -> {
                message.setSenderName(user.getName());
            });
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/code", message);
        }
    }

    // 권한 부여 메시지 처리
    @MessageMapping("/room/{roomId}/grant-permission")
    public void handlePermissionGrant(@DestinationVariable String roomId, PermissionChangeMessage message) {
        Room room = roomService.findRoomById(roomId);
        if (room != null && room.getOwnerId().equals(message.getSenderId())) {
            room.grantWritePermission(message.getTargetUserId());

            // 보내는 사람과 받는 사람의 이름을 찾아서 메시지에 추가
            userRepository.findByUserId(message.getSenderId()).ifPresent(sender -> message.setSenderName(sender.getName()));
            userRepository.findByUserId(message.getTargetUserId()).ifPresent(target -> message.setTargetUserName(target.getName()));

            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/permission", message);
        }
    }
}