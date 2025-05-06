package com.dmu.debug_visual.compiler.python;

import com.dmu.debug_visual.dto.ExecutionStep;
import java.util.ArrayList;
import java.util.List;

/**
 * PythonLogger
 * - 코드 실행 중 상태를 기록하는 유틸 클래스
 * - ExecutionStep 리스트를 축적해 시각화에 활용
 */
public class PythonLogger {

    private final List<ExecutionStep> steps = new ArrayList<>();
    private int stepCount = 0;

    public void log(String operation, List<String> stackState) {
        List<Integer> intStack = stackState.stream()
                .map(Integer::parseInt)
                .toList();
        steps.add(new ExecutionStep(++stepCount, operation, intStack, null));
    }


    public List<ExecutionStep> getSteps() {
        return steps;
    }
}
