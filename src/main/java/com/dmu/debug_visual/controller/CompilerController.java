package com.dmu.debug_visual.controller;

import com.dmu.debug_visual.compiler.InterpreterFactory;
import com.dmu.debug_visual.dto.CompilerRequestDTO;
import com.dmu.debug_visual.dto.ExecutionStep;


import com.dmu.debug_visual.repository.Interpreter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/compiler")
public class CompilerController {

    private final InterpreterFactory interpreterFactory;

    @Operation(
            summary = "코드 실행 요청",
            description = "사용자가 작성한 코드와 언어명을 전달하면 실행 결과를 단계별 상태(ExecutionStep)로 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "실행 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExecutionStep.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "요청 파라미터 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    @PostMapping("/execute")
    public ResponseEntity<List<ExecutionStep>> execute(@RequestBody CompilerRequestDTO request) {
        Interpreter interpreter = interpreterFactory.getInterpreter(request.getLanguage());
        List<ExecutionStep> steps = interpreter.execute(request.getCode(), request.getInput());
        return ResponseEntity.ok(steps);
    }
}
