package com.dmu.debug_visual.websocket.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SystemMessage {
    private String roomId;
    private String senderName;
    private String content; // 예: "OOO님이 입장했습니다."
}