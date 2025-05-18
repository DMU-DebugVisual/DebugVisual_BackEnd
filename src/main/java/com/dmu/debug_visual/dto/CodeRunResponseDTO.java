package com.dmu.debug_visual.dto;

import lombok.Data;

@Data
public class CodeRunResponseDTO {
    private String result; // Python 서버의 stdout
}