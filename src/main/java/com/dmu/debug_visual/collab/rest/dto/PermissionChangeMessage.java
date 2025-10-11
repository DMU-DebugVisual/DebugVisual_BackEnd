package com.dmu.debug_visual.collab.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionChangeMessage {
    private String senderId;     // 권한을 부여하는 사람 (방장)
    private String senderName;
    private String targetUserId; // 권한을 받는 사람
    private String targetUserName;
}