package com.dmu.debug_visual.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data // getter, setter, toString, equals, hashCode 생략
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionStep {
    private int step;                  // 실행 단계 번호
    private String operation;          // 예: append, pop, swap 등
    private List<Integer> stack;       // 현재 스택 상태
    private List<Integer> highlight;   // 시각화 강조할 위치
}
