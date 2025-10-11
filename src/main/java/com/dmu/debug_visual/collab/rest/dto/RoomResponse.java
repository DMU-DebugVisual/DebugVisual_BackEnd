package com.dmu.debug_visual.collab.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

// 방 생성 후 반환할 데이터
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponse {
    private String roomId;
    private String roomName;
    private String ownerId;
    private String defaultSessionId;
}