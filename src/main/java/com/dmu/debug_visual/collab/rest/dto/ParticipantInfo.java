package com.dmu.debug_visual.collab.rest.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 참여자 한 명의 정보를 담는 DTO
 */
@Getter
@Builder
public class ParticipantInfo {
    private String userId;
    private String userName;
}