package com.dmu.debug_visual.compiler.python;

import java.util.List;

/**
 * PythonParser
 * - 토큰을 기반으로 Python 코드의 실행 흐름을 파악하는 클래스
 * - 반복문, 조건문 등의 블록 구분 및 명령어 해석용으로 확장 예정
 */
public class PythonParser {

    public List<String> parse(List<String> tokens) {
        // TODO: 현재는 토큰 그대로 반환, 향후 AST 생성 등 구조화 가능
        return tokens;
    }
}
