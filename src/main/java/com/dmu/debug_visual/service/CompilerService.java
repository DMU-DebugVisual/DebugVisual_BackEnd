package com.dmu.debug_visual.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class CompilerService {

    public String execute(String language, String code, String input) throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("code_");
        String filename = switch (language.toLowerCase()) {
            case "python" -> "main.py";
            case "java" -> "Main.java";
            case "c"     -> "main.c";
            default -> throw new IllegalArgumentException("지원하지 않는 언어입니다.");
        };

        // 1. 파일 저장
        Files.writeString(tempDir.resolve(filename), code);
        Files.writeString(tempDir.resolve("input.txt"), input);

        // 2. Docker 실행
        String container = switch (language.toLowerCase()) {
            case "python" -> "python-compiler";
            case "java"   -> "java-compiler";
            case "c"      -> "c-compiler";
            default -> throw new IllegalArgumentException("지원하지 않는 언어입니다.");
        };

        ProcessBuilder pb = new ProcessBuilder(
                "docker", "run", "--rm",
                "-v", tempDir.toAbsolutePath() + ":/usr/src/app",
                container,
                "bash", "entrypoint.sh"
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        process.waitFor();
        return output.toString();
    }
}
