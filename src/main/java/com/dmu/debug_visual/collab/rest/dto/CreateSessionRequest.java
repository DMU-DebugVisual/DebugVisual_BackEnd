package com.dmu.debug_visual.collab.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateSessionRequest {
    // 사용자가 입력할 세션의 이름 (예: "main.java", "Test Case 1")
    private String sessionName;
}
