package com.dmu.debug_visual.collab.rest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionResponse {
    private String sessionId;
    private String sessionName;
}