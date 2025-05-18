package com.dmu.debug_visual.controller;

import com.dmu.debug_visual.dto.CodeRunRequestDTO;
import com.dmu.debug_visual.service.CodeExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/code")
@Tag(name = "코드 실행 API", description = "Python, Java, C 언어 코드 실행 기능 제공")
public class CodeController {

    private final CodeExecutionService codeExecutionService;

    @Operation(
            summary = "코드 실행",
            description = "입력받은 코드와 입력값을 언어에 따라 실행하고 결과를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 실행됨"),
            @ApiResponse(responseCode = "400", description = "컴파일 또는 실행 에러", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/run")
    public ResponseEntity<String> runCode(@RequestBody CodeRunRequestDTO requestDTO) {
        String result = codeExecutionService.runCode(
                requestDTO.getCode(),
                requestDTO.getInput(),
                requestDTO.getLang()
        );
        return ResponseEntity.ok(result);
    }
}
