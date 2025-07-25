package com.dmu.debug_visual.code.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "코드 실행 결과 응답")
public class CodeRunResponseDTO {

    @Schema(description = "표준 출력 결과", example = "Hello")
    private String stdout;

    @Schema(description = "표준 에러 출력", example = "")
    private String stderr;

    @Schema(description = "프로세스 종료 코드", example = "0")
    private int exitCode;

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "코드 AST 또는 GPT 설명 (visualize 요청 시)", example = "1+2=3")
    private String ast;
}
