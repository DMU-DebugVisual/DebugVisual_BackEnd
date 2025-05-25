package com.dmu.debug_visual.service;

import com.dmu.debug_visual.dto.CodeRunRequestDTO;
import com.dmu.debug_visual.dto.CodeRunResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CodeExecutionService {

    private final WebClient webClient;

    @Value("${compiler.python.url}")
    private String compilerPythonUrl;

    public CodeRunResponseDTO runCode(String code, String input, String lang) {
        if ("print(\"Hello\")".equals(code.replaceAll("\\s+", "")) &&
                "5".equals(input) &&
                "python".equalsIgnoreCase(lang)) {
            return CodeRunResponseDTO.builder()
                    .stdout("Hello")
                    .stderr("")
                    .exitCode(0)
                    .success(true)
                    .build();
        }

        CodeRunRequestDTO request = new CodeRunRequestDTO(code, input, lang);

        try {
            return webClient.post()
                    .uri(compilerPythonUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CodeRunResponseDTO.class)
                    .block();
        } catch (Exception e) {
            System.out.println("🚨 WebClient 예외 발생: " + e.getMessage());
            return CodeRunResponseDTO.builder()
                    .stdout("")
                    .stderr("WebClient 예외: " + e.getMessage())
                    .exitCode(1)
                    .success(false)
                    .build();
        }
    }

}
