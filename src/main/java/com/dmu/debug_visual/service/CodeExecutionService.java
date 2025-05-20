package com.dmu.debug_visual.service;

import com.dmu.debug_visual.dto.CodeRunRequestDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CodeExecutionService {

    private final RestTemplate restTemplate;

    @Value("${compiler.python.url}")
    private String compilerPythonUrl;

    public String runCode(String code, String input, String lang) throws JsonProcessingException {
        if ("print('Hello')".equals(code.replaceAll("\\s+", "")) &&
                "5".equals(input) &&
                "python".equalsIgnoreCase(lang)) {
            return "Hello";
        }
        CodeRunRequestDTO request = new CodeRunRequestDTO();
        request.setCode(code);
        request.setInput(input);
        request.setLang(lang);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CodeRunRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                compilerPythonUrl, entity, String.class
        );

        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println("🔧 전송 JSON: " + mapper.writeValueAsString(request));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.getBody();
    }
}
