package com.dmu.debug_visual.compiler.python;


import com.dmu.debug_visual.dto.ExecutionStep;
import com.dmu.debug_visual.repository.Interpreter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PythonInterpreter implements Interpreter {

    // 사용 언어 확인
    @Override
    public String getLanguage() {
        return "python";
    }

    @Override
    public List<ExecutionStep> execute(String code, String input) {
        List<ExecutionStep> steps = new ArrayList<>();

        try {
            File codeFile = createTempPythonFile(code);
            Process process = runPythonProcess(codeFile, input);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                int step = 1;

                while ((line = reader.readLine()) != null) {
                    List<Integer> stack = parseStackFromLine(line);
                    steps.add(new ExecutionStep(step++, "print", stack, null));
                }
            }

            process.waitFor();
        } catch (Exception e) {
            // TODO: log properly
            e.printStackTrace();
        }

        return steps;
    }

    private File createTempPythonFile(String code) throws IOException {
        File file = File.createTempFile("code", ".py");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(code);
        }
        return file;
    }

    private Process runPythonProcess(File codeFile, String input) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("python3", codeFile.getAbsolutePath());
        Process process = pb.start();

        if (input != null && !input.isEmpty()) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(input);
                writer.flush();
            }
        }

        return process;
    }

    private List<Integer> parseStackFromLine(String line) {
        try {
            return Arrays.stream(
                            line.replaceAll("[\\[\\]]", "")
                                    .trim()
                                    .split(",")
                    )
                    .filter(s -> !s.isEmpty())
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList(); // 실패하면 빈 리스트
        }
    }
}
