package com.dmu.debug_visual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CodeRunRequestDTO {

    @Schema(description = "사용자가 작성한 소스코드", example = "print('Hello')")
    private String code;

    @Schema(description = "표준 입력값", example = "5")
    private String input;

    @Schema(description = "언어 종류 (python, java, c)", example = "python")
    private String lang;
}
