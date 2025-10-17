package com.dmu.debug_visual.collab.rest.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 방의 최신 상태(방 이름, 방장, 참여자 목록) 정보를 담는 DTO.
 * 사용자가 입장/퇴장할 때마다 이 객체가 /topic/room/{roomId}/system 으로 전송됩니다.
 */
@Getter
@Builder
public class RoomStateUpdate {
    private String roomName;
    private ParticipantInfo owner; // 방장 정보
    private List<ParticipantInfo> participants; // 참여자 목록 (방장을 제외한 나머지)
}
