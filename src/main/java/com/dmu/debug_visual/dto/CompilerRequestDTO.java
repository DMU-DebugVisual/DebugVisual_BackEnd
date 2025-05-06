package com.dmu.debug_visual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CompilerRequestDTO {

    @Schema(description = "실행할 언어 (예: python, c)", example = "python")
    private String language;

    @Schema(description = "실행할 코드", example = "stack = []\nstack.append(1)\nprint(stack)")
    private String code;

    @Schema(description = "입력값 (stdin)", example = "")
    private String input;
}
