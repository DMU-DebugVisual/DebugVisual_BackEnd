package com.dmu.debug_visual.repository;

import com.dmu.debug_visual.dto.ExecutionStep;

import java.util.List;

public interface Interpreter {
    List<ExecutionStep> execute(String code, String input);
    String getLanguage(); // "python", "c" 등
}

