package com.dmu.debug_visual.dto;

import lombok.Data;

@Data
public class CodeRequest {
    private String language; // "python", "java", "c" → 선택적
    private String code;     // 실행할 코드
    private String input;    // 표준 입력
}
