package com.dmu.debug_visual.community.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponseDTO {
    private Long id;
    private String targetType; // "POST" or "COMMENT"
    private Long targetId;
    private String reporterName;
    private String reason;
    private LocalDateTime reportedAt;

    // 선택적으로 대상 내용 미리보기
    private String targetPreview;
}
