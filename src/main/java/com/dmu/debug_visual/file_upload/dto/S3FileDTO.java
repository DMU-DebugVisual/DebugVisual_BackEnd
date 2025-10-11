package com.dmu.debug_visual.file_upload.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class S3FileDTO {
    private String fileName;    // 원본 파일 이름
    private String fileUrl;     // S3에 저장된 파일 URL
    private LocalDateTime uploadDate; // 업로드 날짜
    private Long fileSize;      // 파일 크기 (bytes)
}