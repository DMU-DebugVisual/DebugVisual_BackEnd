package com.dmu.debug_visual.compiler;

import com.dmu.debug_visual.repository.Interpreter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InterpreterFactory {

    private final Map<String, Interpreter> interpreterMap = new HashMap<>();

    public InterpreterFactory(List<Interpreter> interpreters) {
        for (Interpreter interpreter : interpreters) {
            interpreterMap.put(interpreter.getLanguage().toLowerCase(), interpreter);
        }
    }

    public Interpreter getInterpreter(String language) {
        Interpreter interpreter = interpreterMap.get(language.toLowerCase());
        if (interpreter == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        return interpreter;
    }
}
