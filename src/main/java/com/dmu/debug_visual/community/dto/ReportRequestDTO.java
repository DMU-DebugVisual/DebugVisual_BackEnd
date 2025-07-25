package com.dmu.debug_visual.community.dto;

import lombok.Data;

@Data
public class ReportRequestDTO {
    private String targetType; // "POST" or "COMMENT"
    private Long targetId;
    private String reason;
}
