package com.dmu.debug_visual.compiler.python;

import java.util.List;

/**
 * PythonLexer
 * - Python 코드의 텍스트를 토큰(또는 줄 단위)으로 분리하는 클래스
 * - 향후 코드 분석 정확도를 높이기 위해 Lexer 기능으로 분리 예정
 */
public class PythonLexer {

    public List<String> tokenize(String code) {
        // TODO: 현재는 줄 단위 분리, 향후 문법 단위로 개선
        return List.of(code.split("\n"));
    }
}
