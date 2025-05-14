package com.dmu.debug_visual.controller;

import com.dmu.debug_visual.dto.CodeRequestDTO;
import com.dmu.debug_visual.service.CompilerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// controller/CompilerController.java
@RestController
@RequestMapping("/api/compile")
@RequiredArgsConstructor
@Tag(name = "컴파일러 API", description = "언어별 코드 실행 API")
public class CompilerController {

    private final CompilerService compilerService;

    @Operation(summary = "코드 실행", description = "언어와 코드를 입력받아 실행 결과를 반환합니다.")
    @PostMapping("/{language}")
    public ResponseEntity<String> compile(
            @PathVariable String language,
            @RequestBody CodeRequestDTO request) {

        try {
            String result = compilerService.execute(language, request.getCode(), request.getInput());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("실행 실패: " + e.getMessage());
        }
    }
}
