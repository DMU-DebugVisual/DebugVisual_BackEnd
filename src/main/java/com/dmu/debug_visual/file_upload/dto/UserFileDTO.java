package com.dmu.debug_visual.file_upload.dto;

import lombok.Builder;
import lombok.Getter;

//import java.time.LocalDateTime; // 필요 시 추가

@Getter
@Builder
public class UserFileDTO {
    private String fileUUID;
    private String originalFileName;
    // 필요에 따라 파일 URL, 생성일자 등 추가 가능
    // private String fileUrl;
    // private LocalDateTime createdAt;
}