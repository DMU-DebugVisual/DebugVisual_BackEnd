package com.dmu.debug_visual.collab.websocket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeMessage {
    private String senderId; // 메시지를 보낸 사람의 ID
    private String senderName;
    private String content;  // 전송할 코드 내용
}