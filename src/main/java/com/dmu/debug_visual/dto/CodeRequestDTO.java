package com.dmu.debug_visual.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CodeRequestDTO {
    private String code;     // 실행할 코드
    private String input;    // 표준 입력
}
