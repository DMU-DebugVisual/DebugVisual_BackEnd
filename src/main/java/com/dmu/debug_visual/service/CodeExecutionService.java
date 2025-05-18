package com.dmu.debug_visual.service;

import com.dmu.debug_visual.dto.CodeRunRequestDTO;
import lombok.RequiredArgsConstructor;
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

    public String runCode(String code, String input, String lang) {
        CodeRunRequestDTO request = new CodeRunRequestDTO();
        request.setCode(code);
        request.setInput(input);
        request.setLang(lang);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CodeRunRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:5050/run", entity, String.class
        );

        return response.getBody();
    }
}
