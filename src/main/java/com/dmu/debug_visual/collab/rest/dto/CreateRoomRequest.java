package com.dmu.debug_visual.collab.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 방 생성 요청 시 받을 데이터
@Getter
@NoArgsConstructor
public class CreateRoomRequest {
    private String roomName;
}