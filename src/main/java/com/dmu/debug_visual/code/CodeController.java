package com.dmu.debug_visual.code;

import com.dmu.debug_visual.code.dto.CodeRunRequestDTO;
import com.dmu.debug_visual.code.dto.CodeRunResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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

    private final ObjectMapper objectMapper;


    @PostMapping("/run")
    @Operation(
            summary = "코드 실행",
            description = "입력받은 코드와 입력값을 언어에 따라 실행하고 결과를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 실행됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CodeRunResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "stdout": "Hello",
                                          "stderr": "",
                                          "exitCode": 0,
                                          "success": true
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "컴파일 또는 실행 에러", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<CodeRunResponseDTO> runCode(@RequestBody CodeRunRequestDTO requestDTO) {
        CodeRunResponseDTO response = codeExecutionService.runCode(
                requestDTO.getCode(),
                requestDTO.getInput(),
                requestDTO.getLang()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/visualize")
    @Operation(
            summary = "코드 시각화 (GPT 설명 포함)",
            description = "코드를 실행하고 실행 결과 및 GPT 기반 AST 설명을 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 실행됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CodeRunResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "stdout": "Hello",
                                      "stderr": "",
                                      "exitCode": 0,
                                      "success": true,
                                      "ast": "1+2=3"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "컴파일 또는 실행 에러", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<CodeRunResponseDTO> visualizeCode(@RequestBody CodeRunRequestDTO requestDTO) {
        CodeRunResponseDTO response = codeExecutionService.visualizeCode(
                requestDTO.getCode(),
                requestDTO.getInput(),
                requestDTO.getLang()
        );
        return ResponseEntity.ok(response);
    }
}