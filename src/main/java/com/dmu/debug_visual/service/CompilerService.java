package com.dmu.debug_visual.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@RequiredArgsConstructor
public class CompilerService {

    private static final Logger log = LoggerFactory.getLogger(CompilerService.class);

    // 프로젝트 루트 기준 상대경로로 설정 (Mac, Linux, Windows 모두 호환)
    private static final String BASE_DIR = "/Users/hanminjeong/Desktop/Debug_Visual/docker";

    public String execute(String language, String code, String input) {
        String filename = switch (language.toLowerCase()) {
            case "python" -> "main.py";
            case "java" -> "Main.java";
            case "c" -> "main.c";
            default -> throw new IllegalArgumentException("지원하지 않는 언어입니다.");
        };

        Path codePath = Paths.get(BASE_DIR, filename);
        Path inputPath = Paths.get(BASE_DIR, "input.txt");

        try {
            Files.writeString(codePath, code, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(inputPath, input, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("📄 파일 쓰기 실패", e);
            return "파일 쓰기 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }

        try {
            String container = switch (language.toLowerCase()) {
                case "python" -> "python-compiler";
                case "java" -> "java-compiler";
                case "c" -> "c-compiler";
                default -> throw new IllegalArgumentException("지원하지 않는 언어입니다.");
            };

            String langPath = BASE_DIR + "/" + language + "/entrypoint.sh";

            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "run", "--rm",
                    "-v", BASE_DIR + ":/usr/src/app",
                    "-v", langPath + ":/usr/src/app/entrypoint.sh",
                    container,
                    "bash", "/usr/src/app/lang/entrypoint.sh"
            );

            log.info("🐳 Docker 실행: {}", String.join(" ", pb.command()));

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            log.info("🔁 Docker 컨테이너 종료 (코드: {})", exitCode);

            return output.toString();
        } catch (Exception e) {
            log.error("🐋 도커 실행 실패", e);
            return "도커 실행 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }
}
